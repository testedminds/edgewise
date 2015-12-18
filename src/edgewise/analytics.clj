(in-ns 'edgewise.core)

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

(defn props
  ([t]
   (if (seq (:vertex t))
     (map #(dissoc % :inE :outE) (select-vals (:vertex-data (:g t)) (:vertex t)))
     (select-vals (:edge-data (:g t)) (:edge t))))
  ([t & keys]
   (mapv #(select-vals % keys) (props t))))

(defn- paths [g dest-id path visited]
  (let [vertex (:inV (peek path))]
    (if (= dest-id vertex) [(rest path)]
        (let [out-e-props (-> (v g vertex) outE props)
              valid-edge-props (filter #(not (contains? visited (:inV %))) out-e-props)]
          (mapcat #(paths g dest-id (conj path %) (conj visited (:inV %))) valid-edge-props)))))

(defn findpaths [g source-label dest-label]
  (let [source-id (label-index g source-label)
        dest-id   (label-index g dest-label)]
    (paths g dest-id [{:inV source-id}] #{source-id})))
