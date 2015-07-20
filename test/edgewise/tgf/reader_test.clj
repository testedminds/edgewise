(ns edgewise.tgf.reader-test
  (:require [clojure.test :refer :all]
            [edgewise.tgf.reader :refer :all]
            [edgewise.graph :refer :all]
            [edgewise.traversal :refer :all]
            [edgewise.analytics :refer :all]))

(deftest should-read-well-formed-tgf-from-file
  (let [g (file->tgf "data/flowrank.tgf")]
    (is (= 8 (-> g v :vertex count)))
    (is (= [6 7] (-> g (v 8) outE inV :vertex sort)))))

(deftest should-read-tgf-from-string
  (let [str "1 Mike Ditka\n2 DA BEARS\n3 Chicago\n#\n1 2 coaches\n1 3 lives in\n"
        g (string->tgf str)]
    (is (= [["Chicago"] ["DA BEARS"]]
           (sort (-> g (v 1) outE inV (props :label)))))
    (is (= [{:label "DA BEARS", :_id 2} {:label "Chicago", :_id 3}]
           (-> g (v 1) outE inV props)))
    (is (= [["coaches" 1] ["lives in" 2]]
           (-> g (v 1) outE (props :label :_id))))))
