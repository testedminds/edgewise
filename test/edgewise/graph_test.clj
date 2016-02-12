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
    (is (= [["MIA"]] (-> (v g 0) out (props :label))))
    (is (= [["ORD" 0] ["LAX" 2]] (-> (v g 1) in (props :label :_id))))
    (is (= 3 (count (:vertex (v g)))))))

(deftest adding-edges-by-id-is-equivalent-to-label
  (let [g (-> (empty-graph)
              (add-vertex "ORD")
              (add-vertex "MIA")
              (add-vertex "LAX")
              (add-edge 0 1)
              (add-edge 2 1))]
    (is (= [["MIA"]] (-> g (v (label-index g "ORD")) out (props :label))))
    (is (= [["ORD" 0] ["LAX" 2]] (-> g (v (label-index g "MIA")) in (props :label :_id))))
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

(deftest should-traverse-edges-with-predicates
  ;; traverse a co-starred / co-directed network using predicates
  (let [g (-> (empty-graph)
              (add-bi-edge "Christopher Walken" "Sean Penn" {:movie "At Close Range" :year 1986 :job "acted"})
              (add-bi-edge "Sean Penn" "Kevin Bacon" {:movie "Mystic River" :year 2003 :job "acted"})
              (add-bi-edge "Quentin Tarantino" "Robert Rodriguez" {:movie "Four Rooms" :year 1995 :job "directed"}))]
    (testing "Finding the co-stars in all movies made before 2000:"
      (is (= #{"Christopher Walken" "Sean Penn"} (-> (v g)
                                                     (out #(and (< (:year %) 2000)
                                                                (= (:job %) "acted")))
                                                     (props :label)
                                                     flatten
                                                     set))))
    (testing "Finding the co-directors of all movies made in the nineties:"
      (is (= #{"Quentin Tarantino" "Robert Rodriguez"} (-> (v g)
                                                           (out #(and (>= (:year %) 1990)
                                                                      (<= (:year %) 1999)
                                                                      (= (:job %) "directed")))
                                                           (props :label)
                                                           flatten
                                                           set))))))
