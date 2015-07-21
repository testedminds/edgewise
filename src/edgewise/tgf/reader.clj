(in-ns 'edgewise.tgf)
(import '[java.io BufferedReader StringReader])
(require '[edgewise.core :refer :all])

(defn- line->vertex [g line]
  (let [[x label] (rest (re-matches #"(\d+) (.+)$" line))]
    (add-vertex g (Integer. x) label)))

(defn- line->edge [g line]
  (let [[x y lbl] (rest (re-matches #"(\d+) (\d+)( .+)?$" line))
        label (if lbl (clojure.string/trim lbl) "")]
    (add-edge g (Integer. x) (Integer. y) {:label label})))

(defn- next-line [[line & rest] {:keys [g line-fn] :as config}]
  #(cond (nil? line) g
     (= line "#") (next-line rest (assoc config :line-fn line->edge))
     :else
     (next-line rest (assoc config :g (line-fn g line)))))

;; Parsing TGF amounts to a simple FSM with two states, :vertex and :edge.
;; Mutually recursive functions work well for this.
(defn- parse-tgf
  ([reader] (parse-tgf reader (empty-graph)))
  ([reader g]
   (with-open [rdr reader]
     (trampoline next-line (line-seq rdr) {:g g :line-fn line->vertex}))))

(defmulti read-tgf
  "Parses TGF strings of the form:
   1 Mike Ditka\n2 DA BEARS\n3 Chicago\n#\n1 2 coaches\n1 3 lives in"
  type)

(defmethod read-tgf java.io.File [f]
  (parse-tgf (clojure.java.io/reader f)))

(defmethod read-tgf java.lang.String [str]
  (parse-tgf (BufferedReader. (StringReader. str))))
