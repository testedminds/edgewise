(ns edgewise.pagerank.diffusion
  (:require [edgewise.core :refer :all]
            [edgewise.pagerank.convergence :as epc]))

(defn- vertex-rank
  [new-rank-increments data v-id]
  (let [rank (get new-rank-increments v-id 0)
        to-all (:all new-rank-increments)
        c (:constant data)
        d (:damping data)]
    (assoc-in data [:rank v-id] (+ c (* d (+ rank to-all))))))

(defn- rank-to-neighbors
  [data v-id]
  (let [rank ((:rank data) v-id)
        neighbor-ids ((:g data) v-id)
        num-neighbors (count neighbor-ids)]
    (if (zero? num-neighbors)
      {:all (/ rank (:vertex-count data))}
      (reduce #(assoc %1 %2 (/ rank num-neighbors)) {} neighbor-ids))))

(defn- increment-rank
  [data]
  (let [all-v-ids (keys (:g data))
        new-rank-increments (reduce #(merge-with + %1 (rank-to-neighbors data %2)) {} all-v-ids)]
    (-> (reduce #(vertex-rank new-rank-increments %1 %2) data all-v-ids)
        (update :iteration inc))))

(defn diffusion
  [g damping-factor pred]
  (let [ids (:vertex (v g))
        n (count ids)
        data {:rank (reduce (fn [acc id] (assoc acc id (/ 1.0 n))) {:all 0} ids)
              :constant (/ (- 1 damping-factor) n)
              :g (adjacency-list g)
              :damping damping-factor
              :iteration 0
              :vertex-count n}
        start (. System (nanoTime))
        result (epc/converge increment-rank data pred)
        stop (. System (nanoTime))
        runtime (/ (double (- stop start)) 1000000.0)
        rank (dissoc (:rank result) :all)
        iter (:iteration result)]
    (with-meta rank {:iteration iter
                     :runtime-millis runtime})))

(defn to-convergence
  [max-iterations]
  (fn [[a b]]
    (and (epc/converging? (dissoc (:rank a) :all) (dissoc (:rank b) :all))
         (< (:iteration b) max-iterations))))

(defn to-max-iterations
  [max-iterations]
  (fn [[a b]] (< (:iteration b) max-iterations)))

