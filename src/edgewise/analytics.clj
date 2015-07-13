(ns edgewise.analytics
  (:require [edgewise.traversal :refer :all]))

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

