(in-ns 'edgewise.core)

;; eigenvector rank algorithm
(defn flowrank [n t]
  (sort-by val >
           (frequencies
            (flatten
             (map :vertex
              (take n (iterate #(inV (outE %)) t)))))))

;; takes a histogram of values (a map) and a traversal
(defn groupcount [hist t]
  (merge-with + hist (frequencies (:vertex t))))

(defn props
  ([t]
   (if (seq (:vertex t))
     (map #(dissoc % :inE :outE) (select-vals (:vertex-data (:g t)) (:vertex t)))
     (select-vals (:edge-data (:g t)) (:edge t))))
  ([t & keys]
   (mapv #(select-vals % keys) (props t))))
