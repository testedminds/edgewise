(in-ns 'edgewise.core)

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
