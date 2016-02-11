(in-ns 'edgewise.core)

;; all fn's in this namespace produce traversals to allow for easy composition.

(defn traversal [g v-ids e-ids]
  {:g g :vertex v-ids :edge e-ids})

(defn v
  ([g] (traversal g
                  (keys (:vertex-data g))
                  []))
  ([g id] (traversal g
                     [id]
                     []))
  ([g property value]
   (traversal g
              [(((:vertex-index g) property) value)]
              [])))

(defn e
  ([g] (traversal g
                  []
                  (keys (:edge-data g))))
  ([g id] (traversal g
                     []
                     [id])))

(defn- edge-traversal
  ([dir t]
   (traversal (:g t)
              []
              (mapcat dir (select-vals (:vertex-data (:g t)) (:vertex t)))))
  ([dir t pred]
   (let [edge-ids (mapcat dir (select-vals (:vertex-data (:g t)) (:vertex t)))
         all-edges (:edge-data (:g t))]
     (traversal (:g t)
                []
                (map :_id (filter pred (select-vals all-edges edge-ids)))))))

(defn in-e
  ([t] (edge-traversal :in-e t))
  ([t pred] (edge-traversal :in-e t pred)))

;; takes a predicate like #(= "acted in" (:type %)) for any edge property.
(defn out-e
  ([t] (edge-traversal :out-e t))
  ([t pred] (edge-traversal :out-e t pred)))

(defn in-v [t]
  (traversal (:g t)
             (map :in-v (select-vals (:edge-data (:g t)) (:edge t)))
             []))

(defn out-v [t]
  (traversal (:g t)
             (map :out-v (select-vals (:edge-data (:g t)) (:edge t)))
             []))
