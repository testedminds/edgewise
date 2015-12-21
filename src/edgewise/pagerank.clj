(in-ns 'edgewise.core)

(defn- rank-increment [data v-id change]
  (let [rank ((:rank data) v-id)]
    (assoc-in data [:rank v-id] (+ rank change))))

(defn- rank-neighbors [data v-id]
  (let [vertex (v (:g data) v-id)
        rank ((:rank data) v-id)
        neighbors (-> vertex outE inV (props :_id) flatten)
        num-neighbors (count neighbors)]
    ;; if v has any outgoing edges, divide current rank equally among them.
    (if (zero? num-neighbors) data
        (let [change (/ rank num-neighbors)]
          (reduce #(rank-increment %1 %2 change) data neighbors)))))

(defn- new-rank [data v-id]
  (let [rank ((:rank data) v-id)
        c (:constant data)
        d (:damping data)]
    (assoc-in data [:rank v-id] (+ c (* d rank)))))

(defn- increment-rank [data]
  (let [vertices (:vertex (v (:g data)))
        updates (reduce rank-neighbors data vertices)]
    (reduce new-rank updates vertices)))

;;;;;;;;;;;

;; map over all vertices to get a vector of a map of changes.
;;; get the ids of the outgoing edges and incoming vertices
;;; compute the change in rank for these neighbors contributed by the vertex.
;; at the end, call merge-with + on the results with the original data
;; should be able to pmap the fn that creates the update maps.

;; updates [{1 0.43 2 0.34 3 0.01} {1 0.32 4 0.01 5 0.01}])

(defn- rank-to-neighbors [data v-id]
  (let [vertex (v (:g data) v-id)
        rank ((:rank data) v-id)
        neighbor-ids (-> vertex outE inV (props :_id) flatten)
        num-neighbors (count neighbor-ids)]
    (if (zero? num-neighbors)
      (let [allocation (/ rank (count (:vertex (v (:g data)))))]  ; if a node has no outE, divide current rank equally among all the nodes
        {:all allocation})
      (let [allocation (/ rank num-neighbors)] ; if v has any outE, divide current rank equally among them.
        (reduce #(assoc %1 %2 (+ ((:rank data) %2) allocation)) {} neighbor-ids)))))

(defn- rank [new-rank-increments data v-id]
  (let [r (get new-rank-increments v-id 0)
        to-all (:all new-rank-increments)
        c (:constant data)
        d (:damping data)]
    (assoc-in data [:rank v-id] (+ c (* d (+ r to-all))))))

(defn- new-increment-rank [data]
  (let [all-v-ids (:vertex (v (:g data)))
        updates (map #(rank-to-neighbors data %) all-v-ids)
        new-rank-increments (apply merge-with + updates)]
    ;; at this point, some of the v's have updated values
    ;; reduce all-v-ids to compute a new rank in data with a fn that looks up the new value from the increments (or 0), and then
    ;; sets the vertex rank to the new-rank, taking the damping-factor and allocations to all pages into account.
    (reduce #(rank new-rank-increments %1 %2) data all-v-ids)))

;; A PageRank of 0.5 means there is a 50% chance that a person clicking on a random link
;; will be directed to the document with the 0.5 PageRank.
(defn diffusion [g damping-factor n]
  (let [vertex-ids (:vertex (v g))
        constant (/ (- 1 damping-factor) (count vertex-ids))
        rank (reduce (fn [acc x] (assoc acc x 0.00)) {:all 0} vertex-ids)
        data {:rank rank :constant constant :g g :damping damping-factor}]
    (sort-by val > (:rank (nth (iterate new-increment-rank data) n)))))

;; prints the rank with the label instead of the id
(defn rank-by-labels [g ranks]
  (map (fn [[id rank]] [(-> (v g id) (props :label) ffirst) rank]) ranks))

;; https://en.wikipedia.org/wiki/PageRank
;; https://en.wikipedia.org/wiki/PageRank#/media/File:PageRanks-Example.svg
(def example (-> (empty-graph)
                 (add-edge "B" "C")
                 (add-edge "C" "B")
                 (add-edge "D" "A")
                 (add-edge "D" "B")
                 (add-edge "E" "B")
                 (add-edge "E" "D")
                 (add-edge "E" "F")
                 (add-edge "F" "E")
                 (add-edge "F" "B")
                 (add-edge "G" "E")
                 (add-edge "H" "E")
                 (add-edge "I" "E")
                 (add-edge "I" "B")
                 (add-edge "J" "E")
                 (add-edge "J" "B")
                 (add-edge "K" "E")
                 (add-edge "K" "B")))

;; (-> (v g 1) outE inV inE outV (props :label))
;;(diffusion example 0.85 5)



