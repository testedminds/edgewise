(ns edgewise.tgf.reader-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.tgf :refer :all]))

(deftest should-read-tgf-from-file
  (let [g (read-tgf (java.io.File. "data/flowrank.tgf"))]
    (is (= 8 (-> g v :vertex count)))
    (is (= [6 7] (-> g (v 8) outE inV :vertex sort)))))

(deftest should-read-tgf-from-string
  (let [str "1 Mike Ditka\n2 DA BEARS\n3 Chicago\n#\n1 2 coaches\n1 3 lives in\n"
        g (read-tgf str)]
    (is (= [["Chicago"] ["DA BEARS"]]
           (sort (-> g (v 1) outE inV (props :label)))))
    (is (= [{:label "DA BEARS", :_id 2} {:label "Chicago", :_id 3}]
           (-> g (v 1) outE inV props)))
    (is (= [["coaches" 1] ["lives in" 2]]
           (-> g (v 1) outE (props :label :_id))))))

(deftest handle-missing-file
  (is (thrown? java.io.FileNotFoundException (read-tgf (java.io.File. "not-a-file")))))

;; TODO
(deftest handle-malformed-tgf)
