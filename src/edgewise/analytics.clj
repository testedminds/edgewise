(in-ns 'edgewise.core)

(defn props
  ([t]
   (if (seq (:vertex t))
     (map #(dissoc % :inE :outE) (select-vals (:vertex-data (:g t)) (:vertex t)))
     (select-vals (:edge-data (:g t)) (:edge t))))
  ([t & keys]
   (mapv #(select-vals % keys) (props t))))

(defn adjacency-list
  "Create a simplified representation of the graph: {vertex-id [neighbor-v-ids]}"
  [g]
  (let [all-v-ids (:vertex (v g))]
    (reduce #(assoc %1 %2 (-> (v g %2) outE inV (props :_id) flatten)) {} all-v-ids)))
