(in-ns 'edgewise.tgf)

(require '[edgewise.core :refer :all])

(defn ->tgf
  ([g]
   (let [vertices (->> (props (v g) :_id :label)
                       (map (fn [[id lbl]] (str id " " lbl "\n")))
                       (apply str))
         edges (->> (props (e g) :outV :inV :label)
                    (map (fn [[outv inv lbl]]
                           (str outv " " inv
                                (when (not-empty lbl) (str " " lbl)) "\n")))
                    (apply str))]
     (str vertices "#\n" edges)))

  ([g file]
   (with-open [w (clojure.java.io/writer file)]
     (doseq [vertex (->> (props (v g) :_id :label)
                         (map (fn [[id lbl]] (str id " " lbl))))]
       (.write w vertex)
       (.newLine w))
     (.write w "#")
     (.newLine w)
     (doseq [edge (->> (props (e g) :outV :inV :label)
                    (map (fn [[outv inv lbl]]
                           (str outv " " inv
                                (when (not-empty lbl) (str " " lbl))))))]
       (.write w edge)
       (.newLine w)))))
