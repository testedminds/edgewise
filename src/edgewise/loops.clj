(in-ns 'edgewise.core)

(defn self-loop-edges
  "Returns a seq of edges with vertices connected to themselves."
  [g]
  (filter (fn [{:keys [out-v in-v]}] (= out-v in-v)) (vals (:edge-data g))))

(defn self-loop-edge-ids
  "Returns a seq of edge ids for edges with vertices connected to themselves."
  [g]
  (map #(:_id %) (self-loop-edges g)))

(defn self-loop-vertex-ids
  "Returns a seq of ids for vertices that have edges connected to themselves."
  [g]
  (map #(:out-v %) (self-loop-edges g)))

(defn self-loop-vertex-labels
  "Returns a seq of labels for vertices that have edges connected to themselves."
  [g]
  (-> g
      (traversal (self-loop-vertex-ids g) [])
      (props :label)
      flatten))

(defn remove-self-loops
  [g]
  (reduce #(remove-edge %1 %2) g (self-loop-edge-ids g)))
