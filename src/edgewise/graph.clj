(in-ns 'edgewise.core)

;; All fn's return a graph:

(defn empty-graph []
  {:vertex-id 0
   :edge-id 0
   :vertex-data {}
   :vertex-index {:label {}}
   :edge-data {}})

(defn- update-vertex-index [g property value id]
  (let [index (:vertex-index g)
        property-index (index property)
        update (assoc property-index value id)
        new-v-index (assoc index property update)]
    (assoc g :vertex-index new-v-index)))

(defn label-index [g label]
  (((:vertex-index g) :label) label))

(defn add-vertex
  ([g label] (add-vertex g label {}))
  ([g label props]
   (let [id (or (:_id props) (:vertex-id g))
         existing (or ((:vertex-data g) id) (label-index g label))
         v (assoc props :outE [] :inE [] :label label :_id id)
         new-v-data (assoc (:vertex-data g) id v)]
     (if existing g
         (-> (assoc g :vertex-data new-v-data)
             (assoc :vertex-id (inc (:vertex-id g)))
             (update-vertex-index :label label id))))))

(defn- add-edge-by-id [g source-id target-id props]
  (let [next-e-id (:edge-id g)
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
        (assoc :edge-id (inc next-e-id)))))

(defmulti add-edge* (fn [g source-id target-id props] [(type source-id) (type target-id)]))

(defmethod add-edge* [Number Number]
  [g source-id target-id props]
  (add-edge-by-id g source-id target-id props))

(defmethod add-edge* [String String]
  [g source-label target-label props]
  (let [g-with-v (-> (add-vertex g source-label)
                     (add-vertex target-label))
        source-id (label-index g-with-v source-label)
        target-id (label-index g-with-v target-label)]
    (add-edge-by-id g-with-v source-id target-id props)))

(defn add-edge
  ([g source-id target-id props] (add-edge* g source-id target-id props))
  ([g source-id target-id] (add-edge g source-id target-id {})))

(defn add-undirected-edge
  ([g i j props]
   (-> g
       (add-edge i j props)
       (add-edge j i props)))
  ([g i j]
   (-> g
       (add-edge i j {})
       (add-edge j i {}))))

(defn- remove-edge-from-vertex
  [g edge-id vertex-id]
  (let [vertex (-> g :vertex-data (get vertex-id))
        new-oute (remove #{edge-id} (:outE vertex))
        new-ine  (remove #{edge-id} (:inE  vertex))
        new-vertex (-> vertex (assoc :outE new-oute) (assoc :inE new-ine))]
    (assoc-in g [:vertex-data vertex-id] new-vertex)))

(defn remove-edge
  [g edge-id]
  (let [edge ((:edge-data g) edge-id)
        new-edge-data (dissoc (:edge-data g) edge-id)]
    (-> (assoc g :edge-data new-edge-data)
        (remove-edge-from-vertex edge-id (:outV edge))
        (remove-edge-from-vertex edge-id (:inV  edge)))))
