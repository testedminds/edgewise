(ns edgewise.graph-test
  (:require [clojure.test :refer :all]
            [edgewise.analytics :refer :all]
            [edgewise.graph :refer :all]
            [edgewise.traversal :refer :all]
            [edgewise.tgf-reader :refer :all]))

(deftest should-handle-duplicate-vertices
  (let [g (-> (empty-graph)
              (add-vertex 7)
              (add-vertex 7))]
    (is (= 1 (count (:vertex (v g)))))))

(deftest should-add-edges-by-label
  (let [g (-> (empty-graph)
              (add-edge "ORD" "MIA")
              (add-edge "LAX" "MIA"))]
    (is (= [["MIA"]] (-> (v g 1) outE inV (props :label))))
    (is (= [["ORD" 1] ["LAX" 3]] (-> (v g 2) inE outV (props :label :_id))))
    (is (= 3 (count (:vertex (v g)))))))

(deftest adding-edges-by-id-is-equivalent-to-label
  (let [g (-> (empty-graph)
              (add-vertex "ORD")
              (add-vertex "MIA")
              (add-vertex "LAX")
              (add-edge 1 2)
              (add-edge 3 2))]
    (is (= [["MIA"]] (-> (v g 1) outE inV (props :label))))
    (is (= [["ORD" 1] ["LAX" 3]] (-> (v g 2) inE outV (props :label :_id))))
    (is (= 3 (count (:vertex (v g)))))))
