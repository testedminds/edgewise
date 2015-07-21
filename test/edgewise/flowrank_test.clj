(ns edgewise.flowrank-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]
            [edgewise.tgf :refer :all]))

;; define a sample graph based on http://www.ams.org/samplings/feature-column/fcarc-pagerank
(defn build-test-graph []
  (-> (reduce #(add-vertex %1 %2) (empty-graph) (range 1 9))
      (add-edge 1 2)
      (add-edge 1 3)
      (add-edge 2 4)
      (add-edge 3 2)
      (add-edge 3 5)
      (add-edge 4 2)
      (add-edge 4 5)
      (add-edge 4 6)
      (add-edge 5 6)
      (add-edge 5 7)
      (add-edge 5 8)
      (add-edge 6 8)
      (add-edge 7 1)
      (add-edge 7 5)
      (add-edge 7 8)
      (add-edge 8 7)
      (add-edge 8 6)))

;; (flowrank 10 (v g))
;; => ([8 2594] [6 2108] [7 1904] [5 1344] [1 923] [2 867] [3 446] [4 420])

(deftest should-compute-simple-flowrank
  (let [g (build-test-graph)
        rank (flowrank 10 (v g))
        tgf (file->tgf "data/flowrank.tgf")
        tgf-rank (flowrank 10 (v tgf))]
    (is (= (first (first rank)) 8))
    (is (= rank tgf-rank))))
