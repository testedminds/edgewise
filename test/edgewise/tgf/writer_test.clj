(ns edgewise.tgf.writer-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.tgf :refer :all]
            [clojure.java.io :as io]))

(deftest should-write-tgf-as-string
  (let [expected-file (io/file (io/resource "resources/flowrank.tgf"))
        g (read-tgf expected-file)
        tgf-str (->tgf g)
        expected (set (clojure.string/split (slurp expected-file) #"\n"))]
    (is (= expected (set (clojure.string/split tgf-str #"\n"))))))

(deftest should-write-tgf-as-file
  (let [expected-file (io/file (io/resource "resources/flowrank.tgf"))
        g (read-tgf expected-file)
        outfile "/tmp/write-test.tgf"
        tgf-str (->tgf g outfile)
        expected (set (clojure.string/split (slurp expected-file) #"\n"))
        actual (set (clojure.string/split (slurp outfile) #"\n"))]
    (is (= expected actual))))
