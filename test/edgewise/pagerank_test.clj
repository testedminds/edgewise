(ns edgewise.pagerank-test
  (:require [edgewise.core :refer :all]
            [edgewise.pagerank.diffusion :refer :all]
            [edgewise.util :as util]
            [clojure.test :refer :all]))

;; source: https://en.wikipedia.org/wiki/PageRank#/media/File:PageRanks-Example.svg
(def example (-> (empty-graph)
                 (add-edge "B" "C")
                 (add-edge "C" "B")
                 (add-edge "D" "A")
                 (add-edge "D" "B")
                 (add-edge "E" "B")
                 (add-edge "E" "D")
                 (add-edge "E" "F")
                 (add-edge "F" "E")
                 (add-edge "F" "B")
                 (add-edge "G" "E")
                 (add-edge "H" "E")
                 (add-edge "I" "E")
                 (add-edge "I" "B")
                 (add-edge "J" "E")
                 (add-edge "J" "B")
                 (add-edge "K" "E")
                 (add-edge "K" "B")))

(def ranks-after-50-iterations
  {0  0.3843697810
   1  0.3429414534
   4  0.0808856932
   2  0.0390870921
   5  0.0390870921
   3  0.0327814932
   6  0.0161694790
   7  0.0161694790
   8  0.0161694790
   9  0.0161694790
   10 0.0161694790})

(deftest should-compute-pagerank-by-diffusion-method
  (let [actual (diffusion example 0.85 50)
        expected ranks-after-50-iterations]
    (doseq [[k v] actual]
      (is (util/nearly (expected k) (actual k))))))

(deftest should-provide-sorted-view-with-vertex-labels
  (let [ranks (pagerank example 50)]
    (is (= "B" (ffirst ranks)))))

(deftest sum-of-ranks-should-be-nearly-1
  (let [ranks (pagerank example 50)]
    (is (valid-ranks! (diffusion example 0.85 50)))))

(deftest quick-perf
  (dotimes [x 3]
    (let [limit-ms 2000
          start (. System (nanoTime))
          ranks (pagerank example 50000)
          stop (. System (nanoTime))
          runtime (/ (double (- stop start)) 1000000.0)]
      (spit "/tmp/pagerank.csv" (prn-str runtime) :append true)
      (is (< runtime limit-ms)))))
