(ns edgewise.tgf-reader-test
  (:require [clojure.test :refer :all]
            [edgewise.tgf-reader :refer :all]
            [edgewise.graph :refer :all]
            [edgewise.traversal :refer :all]))

(deftest should-read-well-formed-tgf
  (let [g (read-tgf "data/flowrank.tgf" (empty-graph))]
    (is (= 8 (-> (v g) :vertex count)))
    (is (= [6 7] (-> (v g 8) outE inV :vertex sort)))))
