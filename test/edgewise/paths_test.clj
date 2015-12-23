(ns edgewise.paths-test
  (:require [edgewise.core :refer :all]
            [clojure.test :refer :all]))

(def costars
  (-> (empty-graph)
      (add-vertex "David Drew Gallagher")
      (add-vertex "Christopher Walken")
      (add-vertex "Kevin Bacon")
      (add-undirected-edge 0 1 {:name "Kiss Toledo Goodbye" :date "1999" :type "appeared with"})
      (add-undirected-edge 2 0 {:name "We Married Margo" :date "2000" :type "appeared with"})))

(deftest should-find-all-paths
  (is (= ["Kiss Toledo Goodbye" "We Married Margo"]
         (map :name (first (findpaths costars "Christopher Walken" "Kevin Bacon"))))))
