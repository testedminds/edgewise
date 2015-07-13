(ns edgewise.tgf-reader-test
  (:require [clojure.test :refer :all]
            [edgewise.tgf-reader :refer :all]
            [edgewise.graph :refer :all]
            [edgewise.traversal :refer :all]))

(deftest should-read-well-formed-tgf-from-file
  (let [g (file->tgf "data/flowrank.tgf")]
    (is (= 8 (-> (v g) :vertex count)))
    (is (= [6 7] (-> (v g 8) outE inV :vertex sort)))))

(deftest should-read-tgf-from-string
  (let [str "1 Mike Ditka\n2 DA BEARS\n3 Chicago\n#\n1 2 coaches\n1 3 lives in\n"
        g (string->tgf str)]
    (is (= ["Chicago" "DA BEARS"] (-> (v g 1) outE inV (props :label) sort)))))
