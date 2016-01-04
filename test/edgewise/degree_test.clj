(ns edgewise.degree-test
  (:require [clojure.test  :refer :all]
            [edgewise.core :refer :all]
            [edgewise.tgf  :refer :all]))

(deftest degrees-returns-a-map-of-labels-to-a-map-of-in-and-out-degree
  (let [g (read-tgf (java.io.File. "data/flowrank.tgf"))]
    (is (= {:out 3, :in 2} (get (degrees g) "6")))))

;; degree distribution is of the form
{:out {"degree k" "number of vertices in g of out-degree k"}
 :in  {"degree k" "number of vertices in g of in-degree k"}}

(deftest test-key-is-degree-and-val-is-number-of-vertices-of-degree
  (let [g (read-tgf (java.io.File. "data/flowrank.tgf"))
        dd (degree-distribution g)
        num-vertices (count (:vertex (v g)))
        num-edges (count (:edge (e g)))]
    ;; Read the map from val to key. There are:
    ;;   2 vertices with out-degree 1
    ;;   3 vertices with out-degree 2
    ;;   3 vertices with out-degree 3
    (is (= {1 2, 2 3, 3 3} (:out dd)))
    (is (= {1 3, 3 4, 2 1} (:in dd)))
    (testing "sanity"
      (is (= num-vertices (apply + (vals (:out dd)))))
      (is (= num-vertices (apply + (vals (:in dd)))))
      (is (= num-edges (reduce (fn [total [degree num]] (+ total (* degree num))) 0 (seq (:out dd)))))
      (is (= num-edges (reduce (fn [total [degree num]] (+ total (* degree num))) 0 (seq (:in dd))))))))
