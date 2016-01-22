;; gorilla-repl.fileformat = 1

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
(ns edgewise.pagerank-test
  (:require [edgewise.core :refer :all]
            [edgewise.pagerank.diffusion :refer :all]
            [edgewise.util :as util]
            [clojure.test :refer :all]
            [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(def g 
  (-> (empty-graph)
      (add-vertex "Jeff Ramnani" {:location "Chicago" :employer "8th Light"})
      (add-vertex "Bobby Norton" {:location "Chicago" :employer "Tested Minds"})
      (add-edge 0 1 {:label "knows" :created "10/1/2008"})))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/g</span>","value":"#'edgewise.pagerank-test/g"}
;; <=

;; **
;;; Edgewise uses an adjacency map as it's internal graph data structure. It's just a map with a few special keys that allow a vertex to know about its incoming and outgoing edges, and edges to know about their incoming and outgoing vertices.
;; **

;; @@
(pprint g)
;; @@
;; ->
;;; {:vertex-id 2,
;;;  :edge-id 1,
;;;  :vertex-data
;;;  {0
;;;   {:location &quot;Chicago&quot;,
;;;    :employer &quot;8th Light&quot;,
;;;    :outE [0],
;;;    :inE [],
;;;    :label &quot;Jeff Ramnani&quot;,
;;;    :_id 0},
;;;   1
;;;   {:location &quot;Chicago&quot;,
;;;    :employer &quot;Tested Minds&quot;,
;;;    :outE [],
;;;    :inE [0],
;;;    :label &quot;Bobby Norton&quot;,
;;;    :_id 1}},
;;;  :vertex-index {:label {&quot;Jeff Ramnani&quot; 0, &quot;Bobby Norton&quot; 1}},
;;;  :edge-data
;;;  {0 {:label &quot;knows&quot;, :created &quot;10/1/2008&quot;, :outV 0, :inV 1, :_id 0}}}
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Edgewise has a traversal DSL that allows navigation around the graph. We can use the label-index to get a vertex id:
;; **

;; @@
(let [start (label-index g "Jeff Ramnani")]
  (-> g (v start) outE inV (props :label)))

;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;Bobby Norton&quot;</span>","value":"\"Bobby Norton\""}],"value":"[\"Bobby Norton\"]"}],"value":"[[\"Bobby Norton\"]]"}
;; <=

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
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/example</span>","value":"#'edgewise.pagerank-test/example"}
;; <=

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
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/initial-conditions</span>","value":"#'edgewise.pagerank-test/initial-conditions"}
;; <=

;; **
;;; We can now run this test, and change the test to make it pass or fail:
;; **

;; @@
(initial-conditions)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

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
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/should-compute-pagerank-by-diffusion-method</span>","value":"#'edgewise.pagerank-test/should-compute-pagerank-by-diffusion-method"}
;; <=

;; @@
(should-compute-pagerank-by-diffusion-method)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; Similarly, if we change the value of the damping factor, the test will fail: Change the value in the test above and re-evaluate.
;;; 
;;; Recall that @@1-d@@ is the probability of jumping to a random vertex. If we set d to 1, we remove the effect of damping. Without damping, all web surfers would eventually end up on vertices B, or C, and all other vertices would have PageRank zero. In the presence of damping, vertex A effectively links to all pages in the web, even though it has no outgoing links of its own. Page C has a higher PageRank than Page E, even though there are fewer links to C; the one link to C comes from an important page and hence is of high value.
;; **

;; @@
(pprint (pagerank example 10000 1))
;; @@
;; ->
;;; ([&quot;C&quot; 0.6146788990825691]
;;;  [&quot;B&quot; 0.3853211009174309]
;;;  [&quot;H&quot; 0.0]
;;;  [&quot;E&quot; 0.0]
;;;  [&quot;G&quot; 0.0]
;;;  [&quot;A&quot; 0.0]
;;;  [&quot;D&quot; 0.0]
;;;  [&quot;J&quot; 0.0]
;;;  [&quot;F&quot; 0.0]
;;;  [&quot;K&quot; 0.0]
;;;  [&quot;I&quot; 0.0])
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; It would be nice to be able to see the vertex labels as the default key of PageRank, rather than just work with a raw map of vertex ids and ranks:
;; **

;; @@
(deftest should-provide-sorted-view-with-vertex-labels
  (let [ranks (pagerank example 50)]
    (is (= "B" (ffirst ranks)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/should-provide-sorted-view-with-vertex-labels</span>","value":"#'edgewise.pagerank-test/should-provide-sorted-view-with-vertex-labels"}
;; <=

;; @@
(pprint (pagerank example 50))
;; @@
;; ->
;;; ([&quot;B&quot; 0.38436978095287694]
;;;  [&quot;C&quot; 0.3429414533690358]
;;;  [&quot;E&quot; 0.08088569323450434]
;;;  [&quot;D&quot; 0.039087092099970126]
;;;  [&quot;F&quot; 0.039087092099970126]
;;;  [&quot;A&quot; 0.032781493159347676]
;;;  [&quot;H&quot; 0.016169479016858935]
;;;  [&quot;G&quot; 0.016169479016858935]
;;;  [&quot;J&quot; 0.016169479016858935]
;;;  [&quot;K&quot; 0.016169479016858935]
;;;  [&quot;I&quot; 0.016169479016858935])
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We also said that the sum of all rank values is 1 (or something very close to it):
;; **

;; @@
(deftest sum-of-ranks-should-be-nearly-1
  (is (valid-ranks! (pagerank example 50 0.85))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/sum-of-ranks-should-be-nearly-1</span>","value":"#'edgewise.pagerank-test/sum-of-ranks-should-be-nearly-1"}
;; <=

;; @@
(sum-of-ranks-should-be-nearly-1)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

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
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;edgewise.pagerank-test/simple-perf-test</span>","value":"#'edgewise.pagerank-test/simple-perf-test"}
;; <=

;; @@
(simple-perf-test)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(clojure.string/split (slurp "/tmp/pagerank.csv") #"\n")
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-string'>&quot;1029.144205&quot;</span>","value":"\"1029.144205\""},{"type":"html","content":"<span class='clj-string'>&quot;1127.411888&quot;</span>","value":"\"1127.411888\""},{"type":"html","content":"<span class='clj-string'>&quot;988.571437&quot;</span>","value":"\"988.571437\""},{"type":"html","content":"<span class='clj-string'>&quot;982.472479&quot;</span>","value":"\"982.472479\""},{"type":"html","content":"<span class='clj-string'>&quot;872.387859&quot;</span>","value":"\"872.387859\""},{"type":"html","content":"<span class='clj-string'>&quot;957.154324&quot;</span>","value":"\"957.154324\""},{"type":"html","content":"<span class='clj-string'>&quot;980.449063&quot;</span>","value":"\"980.449063\""},{"type":"html","content":"<span class='clj-string'>&quot;856.287843&quot;</span>","value":"\"856.287843\""},{"type":"html","content":"<span class='clj-string'>&quot;1001.150355&quot;</span>","value":"\"1001.150355\""},{"type":"html","content":"<span class='clj-string'>&quot;1047.434499&quot;</span>","value":"\"1047.434499\""},{"type":"html","content":"<span class='clj-string'>&quot;871.374191&quot;</span>","value":"\"871.374191\""},{"type":"html","content":"<span class='clj-string'>&quot;935.554417&quot;</span>","value":"\"935.554417\""},{"type":"html","content":"<span class='clj-string'>&quot;968.713945&quot;</span>","value":"\"968.713945\""},{"type":"html","content":"<span class='clj-string'>&quot;894.072959&quot;</span>","value":"\"894.072959\""},{"type":"html","content":"<span class='clj-string'>&quot;953.042524&quot;</span>","value":"\"953.042524\""},{"type":"html","content":"<span class='clj-string'>&quot;1016.064801&quot;</span>","value":"\"1016.064801\""},{"type":"html","content":"<span class='clj-string'>&quot;879.17246&quot;</span>","value":"\"879.17246\""},{"type":"html","content":"<span class='clj-string'>&quot;931.949337&quot;</span>","value":"\"931.949337\""},{"type":"html","content":"<span class='clj-string'>&quot;1019.595637&quot;</span>","value":"\"1019.595637\""},{"type":"html","content":"<span class='clj-string'>&quot;928.131289&quot;</span>","value":"\"928.131289\""},{"type":"html","content":"<span class='clj-string'>&quot;950.706926&quot;</span>","value":"\"950.706926\""},{"type":"html","content":"<span class='clj-string'>&quot;1025.776636&quot;</span>","value":"\"1025.776636\""},{"type":"html","content":"<span class='clj-string'>&quot;905.947073&quot;</span>","value":"\"905.947073\""},{"type":"html","content":"<span class='clj-string'>&quot;948.688813&quot;</span>","value":"\"948.688813\""},{"type":"html","content":"<span class='clj-string'>&quot;1059.949227&quot;</span>","value":"\"1059.949227\""},{"type":"html","content":"<span class='clj-string'>&quot;927.681829&quot;</span>","value":"\"927.681829\""},{"type":"html","content":"<span class='clj-string'>&quot;965.881901&quot;</span>","value":"\"965.881901\""},{"type":"html","content":"<span class='clj-string'>&quot;1014.267991&quot;</span>","value":"\"1014.267991\""},{"type":"html","content":"<span class='clj-string'>&quot;898.52651&quot;</span>","value":"\"898.52651\""},{"type":"html","content":"<span class='clj-string'>&quot;934.784102&quot;</span>","value":"\"934.784102\""},{"type":"html","content":"<span class='clj-string'>&quot;1045.143194&quot;</span>","value":"\"1045.143194\""},{"type":"html","content":"<span class='clj-string'>&quot;913.454822&quot;</span>","value":"\"913.454822\""},{"type":"html","content":"<span class='clj-string'>&quot;943.075844&quot;</span>","value":"\"943.075844\""},{"type":"html","content":"<span class='clj-string'>&quot;991.511334&quot;</span>","value":"\"991.511334\""},{"type":"html","content":"<span class='clj-string'>&quot;921.9117&quot;</span>","value":"\"921.9117\""},{"type":"html","content":"<span class='clj-string'>&quot;903.595145&quot;</span>","value":"\"903.595145\""},{"type":"html","content":"<span class='clj-string'>&quot;1197.264312&quot;</span>","value":"\"1197.264312\""},{"type":"html","content":"<span class='clj-string'>&quot;959.125278&quot;</span>","value":"\"959.125278\""},{"type":"html","content":"<span class='clj-string'>&quot;896.552789&quot;</span>","value":"\"896.552789\""},{"type":"html","content":"<span class='clj-string'>&quot;1058.387803&quot;</span>","value":"\"1058.387803\""},{"type":"html","content":"<span class='clj-string'>&quot;901.066785&quot;</span>","value":"\"901.066785\""},{"type":"html","content":"<span class='clj-string'>&quot;894.841335&quot;</span>","value":"\"894.841335\""},{"type":"html","content":"<span class='clj-string'>&quot;971.869555&quot;</span>","value":"\"971.869555\""},{"type":"html","content":"<span class='clj-string'>&quot;983.497622&quot;</span>","value":"\"983.497622\""},{"type":"html","content":"<span class='clj-string'>&quot;956.419181&quot;</span>","value":"\"956.419181\""}],"value":"[\"1029.144205\" \"1127.411888\" \"988.571437\" \"982.472479\" \"872.387859\" \"957.154324\" \"980.449063\" \"856.287843\" \"1001.150355\" \"1047.434499\" \"871.374191\" \"935.554417\" \"968.713945\" \"894.072959\" \"953.042524\" \"1016.064801\" \"879.17246\" \"931.949337\" \"1019.595637\" \"928.131289\" \"950.706926\" \"1025.776636\" \"905.947073\" \"948.688813\" \"1059.949227\" \"927.681829\" \"965.881901\" \"1014.267991\" \"898.52651\" \"934.784102\" \"1045.143194\" \"913.454822\" \"943.075844\" \"991.511334\" \"921.9117\" \"903.595145\" \"1197.264312\" \"959.125278\" \"896.552789\" \"1058.387803\" \"901.066785\" \"894.841335\" \"971.869555\" \"983.497622\" \"956.419181\"]"}
;; <=

;; @@

;; @@
