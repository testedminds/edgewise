;; gorilla-repl.fileformat = 1

;; @@
(ns edgewise.pagerank-test
  (:require [edgewise.core :refer :all]
            [edgewise.pagerank.diffusion :refer :all]
            [edgewise.util :as util]
            [clojure.test :refer :all]
            [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]))
;; @@

;; **
;;; ## Testing PageRank in Edgewise
;;; 
;;; Consider the example graph from the [Wikipedia article](https://en.wikipedia.org/wiki/PageRank#/media/File:PageRanks-Example.svg) on PageRank:
;;; 
;; **

;; **
;;; <div align="center">
;;;   <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/PageRanks-Example.svg/758px-PageRanks-Example.svg.png"
;;;        width="50%"/>
;;; </div>
;;; 
;;; > _Mathematical PageRanks for a simple network, expressed as percentages. PageRank expresses how important each vertex is in the network._
;; **

;; **
;;; We can define this example edgewise in Edgewise: `(add-edge "B" "C")` means _"Add a directed edge from a vertex with label 'B' to the vertex with label 'C', and add those vertices if they don't already exist."_
;; **

;; **
;;; #### Sidebar: Intro to Edgewise
;; **

;; **
;;; Edgewise allows you to create in-memory networks (graphs), navigate among the vertices and edges via composable traversals, and compute network analytics.
;;; 
;;; We can define a sample graph `g` like so:
;; **

;; @@
(def g 
  (-> (empty-graph)
      (add-vertex "Jeff Ramnani" {:location "Chicago" :employer "8th Light"})
      (add-vertex "Bobby Norton" {:location "Chicago" :employer "Tested Minds"})
      (add-edge 0 1 {:label "knows" :created "10/1/2008"})))
;; @@

;; **
;;; Edgewise uses an adjacency map as it's internal graph data structure. It's just a map with a few special keys that allow a vertex to know about its incoming and outgoing edges, and edges to know about their incoming and outgoing vertices.
;; **

;; @@
;;(pprint g)
;; @@

;; **
;;; Edgewise has a traversal DSL that allows navigation around the graph. We can use the label-index to get a vertex id
;; **

;; @@
(let [start (label-index g "Jeff Ramnani")]
  (-> g (v start) outE inV (props :label)))

;; @@

;; @@
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
;; @@

;; **
;;; ### Validating PageRank
;;; 
;;; A challenge with TDD'ing iterated numeric algorithms such as this is determining expected values before the code has been written. The PageRank algorithm is simple enough that this can be worked out in a tool like Excel or even by hand, but in this case, these values were produced by [NetLogo](http://ccl.northwestern.edu/netlogo/models/PageRank):
;; **

;; @@
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
;; @@

;; **
;;; Notice that the values correspond to the percentages in the diagram above: "B" is the most central vertex with a PageRank expressed as a percentage, 38.4%. 
;; **

;; **
;;; Let's see if this actually works by computing PageRank with a "diffusion" approach in which the rank of each page spreads to its neighbors. Intuitively, a vertex is important to the degree to which other highly connected vertices link to it. Initially, every vertex gets the same rank, and the sum of all rank values is 1.
;;; 
;;; More formally, at @@t=0@@, an initial probability distribution is assumed, usually
;;; $$PR(v_i; 0) = \frac{1}{N}$$
;; **

;; **
;;; At each time step, the computation should yield $$PR(v_i;t+1) =$$
;;; 
;;; $$\frac{1-d}{N} + d \sum_{v_j \in S(v_i)} \frac{PR (v_j; t)}{E(v_j)}$$
;; **

;; **
;;; In English:
;;; 
;;; @@v_j \in S(v_i)@@ means "let @@v_j@@ be a member of the set of vertices that link to @@v_i@@".
;;; 
;;; @@{E(v_j)}@@ is the number of outgoing edges from @@v_j@@.
;;; 
;;; @@PR (v_j; t)@@ is the PageRank of @@v_j@@ from the previous iteration (or "time step"), @@t@@.
;;; 
;;; @@d@@ is a damping factor that corrects for sinks, i.e. vertices with no incoming edges, getting a PageRank of zero. Imagine the network represented a set of web pages. If web surfers who start on a random page have an 85% likelihood of choosing a random link from the page they are currently visiting, and a 15% likelihood of jumping to a page chosen at random from the entire web, they will reach Page "B" 38.4% of the time. The fact that a damping factor of 0.85 (85%) corresponds to a 15% likelihood of jumping to an arbitrary page, i.e. @@1-p@@, is worth remembering.
;;; 
;;; In each iteration of the algorithm, every vertex distributes its rank to those vertices to which it has outgoing edges. Each vertex's new PageRank will thus be based on how much rank it receives from each of the vertices that link to it, combined in a weighted average with a baseline amount of rank which each vertex gets at each time step regardless of its neighbors. Over time, this process causes the rank of each page to converge to the actual PageRank values for each page.
;; **

;; **
;;; Let's test the assumption that, initially, every vertex gets the same rank:
;; **

;; @@
(deftest initial-conditions
  (let [damping 0.85
        iterations 0
        actual (diffusion example damping iterations)
        n (-> example :vertex-data keys count)
        expected (/ 1 n)]
    (doseq [[k v] actual]
      (is (util/nearly expected (actual k))))))
;; @@

;; **
;;; We can now run this test, and change the test to make it pass or fail:
;; **

;; @@
(initial-conditions)
;; @@

;; **
;;; And we can verify that the algorithm converges to our expected result:
;; **

;; @@
(deftest should-compute-pagerank-by-diffusion-method
  (let [actual (diffusion example 0.85 50)
        expected ranks-after-50-iterations]
    (doseq [[k v] actual]
      (is (util/nearly (expected k) (actual k))))))
;; @@

;; @@
(should-compute-pagerank-by-diffusion-method)
;; @@

;; **
;;; Similarly, if we change the value of the damping factor, the test will fail: Change the value in the test above and re-evaluate.
;;; 
;;; Recall that @@1-d@@ is the probability of jumping to a random vertex. If we set d to 1, we remove the effect of damping. Without damping, all web surfers would eventually end up on vertices B, or C, and all other vertices would have PageRank zero. In the presence of damping, vertex A effectively links to all pages in the web, even though it has no outgoing links of its own. Page C has a higher PageRank than Page E, even though there are fewer links to C; the one link to C comes from an important page and hence is of high value.
;; **

;; @@
(pagerank example 5000 1)
;; @@

;; **
;;; It would be nice to be able to see the vertex labels as the default key of PageRank, rather than just work with a raw map of vertex ids and ranks:
;; **

;; @@
(deftest should-provide-sorted-view-with-vertex-labels
  (let [ranks (pagerank example 50)]
    (is (= "B" (ffirst ranks)))))
;; @@

;; @@
(pagerank example 50)
;; @@

;; **
;;; We also said that the sum of all rank values is 1 (or something very close to it):
;; **

;; @@
(deftest sum-of-ranks-should-be-nearly-1
  (is (valid-ranks! (pagerank example 50 0.85))))
;; @@

;; @@
(sum-of-ranks-should-be-nearly-1)
;; @@

;; @@
(deftest simple-perf-test
  (dotimes [x 3]
    (let [limit-ms 2000
          start (. System (nanoTime))
          ranks (pagerank example 50000)
          stop (. System (nanoTime))
          runtime (/ (double (- stop start)) 1000000.0)]
      (spit "/tmp/pagerank.csv" (prn-str runtime) :append true)
      (is (< runtime limit-ms)))))
;; @@
