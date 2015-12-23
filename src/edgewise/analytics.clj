(in-ns 'edgewise.core)

(defn props
  ([t]
   (if (seq (:vertex t))
     (map #(dissoc % :inE :outE) (select-vals (:vertex-data (:g t)) (:vertex t)))
     (select-vals (:edge-data (:g t)) (:edge t))))
  ([t & keys]
   (mapv #(select-vals % keys) (props t))))
