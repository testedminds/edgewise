;; gorilla-repl.fileformat = 1

;; @@
(ns edgewise.pagerank.diffusion
  (:require [edgewise.core :refer :all]))
;; @@

;; @@
(declare neighbors)
(declare new-increment-rank)
(declare rank-to-neighbors)
(declare vertex-rank)
;; @@

;; @@
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
;; @@

;; @@
(defn- neighbors
  "create a simplified representation of the graph: {vertex-id [neighbor-v-ids]}"
  [g]
  (let [all-v-ids (:vertex (v g))]
    (reduce #(assoc %1 %2 (-> (v g %2) outE inV (props :_id) flatten)) {} all-v-ids)))
;; @@

;; @@
(defn- new-increment-rank
  [data]
  (let [all-v-ids (keys (:g data))
        new-rank-increments (reduce #(merge-with + %1 (rank-to-neighbors data %2)) {} all-v-ids)]
    (reduce #(vertex-rank new-rank-increments %1 %2) data all-v-ids)))
;; @@

;; @@
(defn- rank-to-neighbors
  [data v-id]
  (let [rank ((:rank data) v-id)
        neighbor-ids ((:g data) v-id)
        num-neighbors (count neighbor-ids)]
    (if (zero? num-neighbors)
      {:all (/ rank (:vertex-count data))}
      (reduce #(assoc %1 %2 (/ rank num-neighbors)) {} neighbor-ids))))
;; @@

;; @@

(defn- vertex-rank
  [new-rank-increments data v-id]
  (let [rank (get new-rank-increments v-id 0)
        to-all (:all new-rank-increments)
        c (:constant data)
        d (:damping data)]
    (assoc-in data [:rank v-id] (+ c (* d (+ rank to-all))))))
;; @@
