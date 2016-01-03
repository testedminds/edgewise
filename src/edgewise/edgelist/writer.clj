(in-ns 'edgewise.edgelist)

(require '[edgewise.core :refer :all])

(defn- edge-data->labels
  [g edge-data]
  (let [outv (:outV edge-data)
        inv  (:inV  edge-data)
        label #(-> (v g %) (props :label) ffirst)]
    [(label outv) (label inv)]))

(defn- edgelist->csv
  [edges file]
  (with-open [w (clojure.java.io/writer file)]
    (doseq [[outv inv] edges]
      (.write w (str outv "," inv))
      (.newLine w))))

(defn g->edgelist
  "Transform the edges of g from ids to labels."
  [g]
  (map #(edge-data->labels g %) (vals (:edge-data g))))

(defn g->edgelist-csv
  "Write the edges of g to a file using vertex labels as identifiers."
  [g file]
  (-> g
      g->edgelist
      (edgelist->csv file)))
