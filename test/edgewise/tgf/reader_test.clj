(ns edgewise.tgf.reader-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.tgf :refer :all]))

(deftest should-read-tgf-from-file
  (let [g (read-tgf (java.io.File. "data/flowrank.tgf"))]
    (is (= 8 (-> g v :vertex count)))
    (is (= [5 6] (-> g (v 7) out-e in-v :vertex sort)))))

(deftest should-read-tgf-from-string
  (let [str "1 Mike Ditka\n2 DA BEARS\n3 Chicago\n#\n1 2 coaches\n1 3 lives in\n"
        g (read-tgf str)]
    (is (= [["Chicago"] ["DA BEARS"]]
           (sort (-> g (v 1) out-e in-v (props :label)))))
    (is (= [{:label "DA BEARS", :_id 2} {:label "Chicago", :_id 3}]
           (-> g (v 1) out-e in-v props)))
    (is (= [["coaches" 0] ["lives in" 1]]
           (-> g (v 1) out-e (props :label :_id))))))

(deftest handle-missing-file
  (is (thrown? java.io.FileNotFoundException (read-tgf (java.io.File. "not-a-file")))))

;; TODO
(deftest handle-malformed-tgf)
