(in-ns 'edgewise.edgelist)

(require '[edgewise.core :refer :all])

(defn- line->edge
  [g csv-dyad]
  (let [[outv inv] (clojure.string/split csv-dyad #",")]
    (add-edge g outv inv)))

(defn edgelist->g
  "Read the edges of g from a csv file in the form label1,label2."
  [file]
  (with-open [r (clojure.java.io/reader file)]
    (reduce line->edge (empty-graph) (line-seq r))))
