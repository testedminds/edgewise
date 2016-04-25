(ns edgewise.edgelist.reader-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.edgelist :refer :all]
            [clojure.java.io :as io]))

(deftest should-read-valid-edgelist
  (let [g (edgelist->g (io/resource "resources/edgelist.txt"))
        id (label-index g "foo")]
    (is (= 4 (count (:vertex-data g))))
    (is (= "bar" (-> g (v id) out (props :label) ffirst)))))
