(ns edgewise.edgelist.writer-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.edgelist :refer :all]
            [clojure.java.io :as io]))

(def graph (-> (empty-graph)
               (add-edge "foo" "bar")
               (add-edge "bar" "baz")
               (add-edge "baz" "quux")))

(deftest should-write-edgelist
  (let [expected (slurp (io/file (io/resource "resources/edgelist.txt")))
        csv "/tmp/edgelist-writer-test.txt"]
    (g->edgelist-csv graph csv)
    (is (= expected (slurp csv)))))
