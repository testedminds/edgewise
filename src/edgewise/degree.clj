(in-ns 'edgewise.core)

(defn- csv->file [data file]
  (with-open [w (clojure.java.io/writer file)]
    (doseq [[k n] data]
      (.write w (str k "," n))
      (.newLine w))))

(defn degrees
  "Returns a map from the :label of the vertices of g to the number of edges of the vertex as [out-degree in-degree]."
  [g]
  (reduce #(assoc %1 (-> g (v %2) (props :label) ffirst)
                  {:out (-> (v g %2) outE props count)
                   :in  (-> (v g %2) inE  props count)})
          {} (keys (:vertex-data g))))

(defn degree-distribution
  "Returns a map from :out and :in degrees k to the number of vertices with degree k in g."
  [g]
  (let [g-degrees (vals (degrees g))]
    {:out (frequencies (pmap :out g-degrees))
     :in  (frequencies (pmap :in  g-degrees))}))

(defn- degree-distribution->csv* [g file direction]
  (-> g
      degree-distribution
      direction
      seq
      (csv->file file)))

(defn degree-distribution->csv [g indegree-file outdegree-file]
  (degree-distribution->csv* g outdegree-file :out)
  (degree-distribution->csv* g indegree-file :in))
