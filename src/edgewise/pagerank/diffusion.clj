(ns edgewise.pagerank.diffusion
  (:require [edgewise.core :refer :all]))

(defn- rank-to-neighbors
  [data v-id]
  (let [rank ((:rank data) v-id)
        neighbor-ids ((:g data) v-id)
        num-neighbors (count neighbor-ids)]
    (if (zero? num-neighbors)
      {:all (/ rank (:vertex-count data))}
      (reduce #(assoc %1 %2 (/ rank num-neighbors)) {} neighbor-ids))))

(defn- rank
  [new-rank-increments data v-id]
  (let [r (get new-rank-increments v-id 0)
        to-all (:all new-rank-increments)
        c (:constant data)
        d (:damping data)]
    (assoc-in data [:rank v-id] (+ c (* d (+ r to-all))))))

(defn- new-increment-rank
  [data]
  (let [all-v-ids (keys (:g data))
        updates (map #(rank-to-neighbors data %) all-v-ids)
        new-rank-increments (apply merge-with + updates)]
    (reduce #(rank new-rank-increments %1 %2) data all-v-ids)))

(defn- neighbors
  "store a simplified representation of the graph: {vertex-id [neighbor-v-ids]}"
  [g]
  (let [all-v-ids (:vertex (v g))]
    (reduce #(assoc %1 %2 (-> (v g %2) outE inV (props :_id) flatten))
            {}
            all-v-ids)))

(defn diffusion
  [g damping-factor num-iterations]
  (let [ids (:vertex (v g))
        n (count ids)
        constant (/ (- 1 damping-factor) n)
        data {:rank (reduce (fn [acc id] (assoc acc id (/ 1.0 n))) {:all 0} ids)
              :constant constant
              :g (neighbors g)
              :damping damping-factor
              :vertex-count n}]
    (-> new-increment-rank
        (iterate data)
        (nth num-iterations)
        :rank
        (dissoc :all))))
