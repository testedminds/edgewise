(ns edgewise.traversal
  (:require [edgewise.util :refer [select-vals]]))

(defn traversal [g v-ids e-ids]
  {:g g :vertex v-ids :edge e-ids})

(defn v
  ([g] (traversal g (keys (:vertex-data g)) []))
  ([g id] (traversal g [id] []))
  ([g property value]
   (traversal g [(((:vertex-index g) property) value)] [])))

(defn outE [t]
  (traversal (:g t) []
    (mapcat :outE
            (select-vals (:vertex-data (:g t)) (:vertex t)))))

(defn inV [t]
  (traversal (:g t)
   (map :inV
        (select-vals (:edge-data (:g t)) (:edge t))) []))
