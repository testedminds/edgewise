(ns edgewise.analytics
  (:require [edgewise.traversal :refer :all]))

;; get the property map for all vertices
(defn props
  ([t]
   (if (seq (:vertex t))
     (map #(dissoc % :inE :outE) (vals (select-keys (:vertex-data (:g t)) (:vertex t))))
     (vals (select-keys (:edge-data (:g t)) (:edge t)))))
  ([t prop] (map prop (props t))))

;; eigenvector rank algorithm
(defn flowrank [n t]
  (sort-by val >
           (frequencies
            (flatten
             (map :vertex
              (rest
               (take n (iterate #(inV (outE %)) t))))))))

;; takes a histogram of values (a map) and a traversal
(defn groupcount [hist t]
  (merge-with + hist (frequencies (:vertex t))))

