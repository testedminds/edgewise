(ns edgewise.loops-test
  (:require [clojure.test :refer :all]
            [edgewise.core :refer :all]))

(def g (-> (empty-graph)
           (add-edge "A" "A")
           (add-edge "B" "B")
           (add-edge "A" "B")
           (add-edge "C" "D")))

(deftest should-find-self-loop-edges
  (is (= [0 1] (map #(:_id %) (self-loop-edges g)))))

(deftest should-find-self-loop-edge-ids
  (is (= [0 1] (self-loop-edge-ids g))))

(deftest should-find-self-loop-vertex-ids
  (is (= [0 1] (self-loop-vertex-ids g))))

(deftest should-find-self-loop-vertex-labels
  (is (= #{"A" "B"} (set (self-loop-vertex-labels g)))))

(deftest should-remove-self-loops
  (is (= 2 (count (keys (:edge-data (remove-self-loops g)))))))
