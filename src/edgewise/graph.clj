(ns edgewise.graph)

(defn empty-graph []
  {:vertex-id 0
   :edge-id 0
   :vertex-data {}
   :vertex-index {:label {}}
   :edge-data {}
   })

(defn update-vertex-index [g property value id]
  (let [index (:vertex-index g)
        property-index (index property)
        update (assoc property-index value id)
        new-v-index (assoc index property update)]
    (assoc g :vertex-index new-v-index)))

(defn add-vertex
  ([g label] (add-vertex g (inc (:vertex-id g)) label))
  ([g id label]
   (let [v {:outE [] :inE [] :label label :_id id}
         new-v-data (assoc (:vertex-data g) id v)]
     (-> (assoc g :vertex-data new-v-data)
         (assoc :vertex-id id)
         (update-vertex-index :label label id)))))

(defn add-edge
  ([g source-id target-id props]
   (let [next-e-id (inc (:edge-id g))
         edge (assoc props :outV source-id :inV target-id :_id next-e-id)
         v (:vertex-data g)
         new-out-e (conj ((v source-id) :outE) next-e-id)
         new-out-v (assoc (v source-id) :outE new-out-e)
         new-in-e  (conj ((v target-id) :inE) next-e-id)
         new-in-v  (assoc (v target-id) :inE new-in-e)
         new-edge-data (assoc (:edge-data g) next-e-id edge)
         new-vertex-data (-> (assoc v source-id new-out-v)
                             (assoc target-id new-in-v))]
     (-> (assoc g :edge-data new-edge-data)
         (assoc :vertex-data new-vertex-data)
         (assoc :edge-id next-e-id))))
  ([g source-id target-id] (add-edge g source-id target-id {})))

