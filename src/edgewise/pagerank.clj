(in-ns 'edgewise.core)

(require '[edgewise.util :as util])

(defn- rank-to-neighbors [data v-id]
  (let [vertex (v (:g data) v-id)
        rank ((:rank data) v-id)
        neighbor-ids (-> vertex outE inV (props :_id) flatten)
        num-neighbors (count neighbor-ids)]
    (if (zero? num-neighbors)
      {:all (/ rank (:vertex-count data))}
      (reduce #(assoc %1 %2 (/ rank num-neighbors)) {} neighbor-ids))))

(defn- rank [new-rank-increments data v-id]
  (let [r (get new-rank-increments v-id 0)
        to-all (:all new-rank-increments)
        c (:constant data)
        d (:damping data)]
    (assoc-in data [:rank v-id] (+ c (* d (+ r to-all))))))

(defn- new-increment-rank [data]
  (let [all-v-ids (:vertex (v (:g data)))
        updates (pmap #(rank-to-neighbors data %) all-v-ids)
        new-rank-increments (apply merge-with + updates)]
    (reduce #(rank new-rank-increments %1 %2) data all-v-ids)))

;; prints the rank with the label instead of the id
(defn- id->label [g ranks]
  (map (fn [[id rank]] [(-> (v g id) (props :label) ffirst) rank]) ranks))

(defn diffusion [g damping-factor num-iterations]
  (let [ids (:vertex (v g))
        n (count ids)
        constant (/ (- 1 damping-factor) n)
        data {:rank (reduce (fn [acc id] (assoc acc id (/ 1.0 n))) {:all 0} ids)
              :constant constant
              :g g
              :damping damping-factor
              :vertex-count n}]
    (-> new-increment-rank
        (iterate data)
        (nth num-iterations)
        :rank
        (dissoc :all))))

(defn valid-ranks! [ranks]
  (let [sum-of-rank (apply + (map second ranks))]
    (assert (util/nearly 1 sum-of-rank) "rank should sum to nearly 1.0")
    ranks))

;; A PageRank of 0.5 means there is a 50% chance that a person clicking on a random link
;; will be directed to the document with the 0.5 PageRank.
(defn pagerank [g num-iterations]
  (->> num-iterations
       (diffusion g 0.85)
       valid-ranks!
       (sort-by val >)
       (id->label g)))
