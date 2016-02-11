(ns edgewise.graph-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]))

(deftest should-ignore-vertices-with-duplicate-labels
  (let [g (-> (empty-graph)
              (add-vertex "foo")
              (add-vertex "foo"))]
    (is (= 1 (count (:vertex (v g)))))))

(deftest should-ignore-vertices-with-duplicate-ids
  (let [g (-> (empty-graph)
              (add-vertex "ORD" {:name "Chicago O'Hare International Airport" :_id 1})
              (add-vertex "MIA" {:name "Miami International Airport" :_id 1}))]
    (is (= [["ORD"]] (-> (v g 1) (props :label))))))

(deftest should-add-edges-by-label
  (let [g (-> (empty-graph)
              (add-edge "ORD" "MIA")
              (add-edge "LAX" "MIA"))]
    (is (= [["MIA"]] (-> (v g 0) out-e in-v (props :label))))
    (is (= [["ORD" 0] ["LAX" 2]] (-> (v g 1) in-e out-v (props :label :_id))))
    (is (= 3 (count (:vertex (v g)))))))

(deftest adding-edges-by-id-is-equivalent-to-label
  (let [g (-> (empty-graph)
              (add-vertex "ORD")
              (add-vertex "MIA")
              (add-vertex "LAX")
              (add-edge 0 1)
              (add-edge 2 1))]
    (is (= [["MIA"]] (-> (v g 0) out-e in-v (props :label))))
    (is (= [["ORD" 0] ["LAX" 2]] (-> (v g 1) in-e out-v (props :label :_id))))
    (is (= 3 (count (:vertex (v g)))))))

(deftest should-add-vertices-with-properties-but-no-id
  (let [g (-> (empty-graph)
              (add-vertex "ORD" {:name "Chicago O'Hare International Airport"}))]
    (is (= [["Chicago O'Hare International Airport"]] (-> (v g 0) (props :name))))))

(deftest should-add-vertices-with-properties-and-possibily-non-numeric-id
  (let [g (-> (empty-graph)
              (add-vertex "ORD" {:name "Chicago O'Hare International Airport" :_id "home"}))]
    (is (= [["Chicago O'Hare International Airport"]] (-> (v g "home") (props :name))))))

(deftest should-remove-edges
  (let [g (-> (empty-graph)
              (add-edge "1" "2")
              (add-edge "2" "3")
              (add-edge "3" "4")
              (remove-edge 0))]
    (is (= 2 (-> g e props count)))
    (is (= 0 (-> g (v 0) out-e :edge count)))))
