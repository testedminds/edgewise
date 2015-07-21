(ns edgewise.tgf.writer-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.tgf :refer :all]))

(deftest should-write-tgf-as-string
  (let [g (read-tgf (java.io.File. "data/flowrank.tgf"))
        tgf-str (->tgf g)
        expected (set (clojure.string/split (slurp "data/flowrank.tgf") #"\n"))]
    (is (= expected (set (clojure.string/split tgf-str #"\n"))))))

(deftest should-write-tgf-as-file
  (let [f "/tmp/write-test.tgf"
        g (read-tgf (java.io.File. "data/flowrank.tgf"))
        tgf-str (->tgf g f)
        expected (set (clojure.string/split (slurp "data/flowrank.tgf") #"\n"))
        actual (set (clojure.string/split (slurp f) #"\n"))]
    (is (= expected actual))))
