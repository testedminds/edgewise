(ns edgewise.tgf-reader
  (:require [edgewise.graph :refer :all]))

(defn- line->vertex [g line]
  (let [[x y] (clojure.string/split line #" ")]
    (add-vertex g (Integer. x) y)))

(defn- line->edge [g line]
  (let [[x y label] (clojure.string/split line #" ")]
    (add-edge g (Integer. x) (Integer. y))))

(defn- next-line [[line & rest] {:keys [g line-fn] :as config}]
  #(cond (nil? line) g
     (= line "#") (next-line rest (assoc config :line-fn line->edge))
     :else
     (next-line rest (assoc config :g (line-fn g line)))))

;; Parsing the TGF file amounts to a simple FSM with two states,
;; :vertex and :edge.
;; Mutually recursive functions work well for this.
(defn read-tgf [file g]
  (with-open [rdr (clojure.java.io/reader file)]
    (trampoline next-line (line-seq rdr) {:g g :line-fn line->vertex})))

