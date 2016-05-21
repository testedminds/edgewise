;; gorilla-repl.fileformat = 1

;; **
;;; ## PageRank in Edgewise
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
;;; > _Mathematical PageRanks for a simple network, expressed as percentages. PageRank is a model of how important each vertex is in the network._
;; **

;; @@
(ns ^:doc pagerank
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :refer :all]
            [gorilla-plot.core :refer :all]
            [gorilla-repl.html :refer :all]
            [gorilla-repl.latex :refer :all]
            [gorilla-repl.table :refer :all]
            [edgewise.core :refer :all]
            [edgewise.pagerank-test :as test]
            [edgewise.pagerank.diffusion :as pd]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; We can define this example edgewise in Edgewise: `(add-edge "B" "C")` means _"Add a directed edge from a vertex with label 'B' to the vertex with label 'C', and add those vertices if they don't already exist."_
;; **

;; @@
(source test/example)
;; @@
;; ->
;;; Source not found
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; "B" is the most central vertex with a PageRank expressed as a percentage, 38.4%. If web surfers who start on a random page have an 85% likelihood of choosing a random link from the page they are currently visiting, and a 15% likelihood of jumping to a page chosen at random from the entire web, they will reach vertex "B" 38.4% of the time.
;; **

;; @@
(pprint (pagerank test/example 50))
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
;;; ### The Diffusion Model
;;; 
;;; Edgewise computes PageRank using a diffusion model in which the rank of each page can be imagined to spread to its neighbors. Intuitively, a vertex is important to the degree to which other popular (high indegree) but not highly gregarious (low outdegree) vertices link to it. Initially, every vertex gets the same rank.
;;; 
;;; More formally, at @@t=0@@, an initial probability distribution is assumed, usually
;;; $$PR(v_i; 0) = \frac{1}{N}$$
;; **

;; @@
(pprint (pagerank test/example 0))
;; @@
;; ->
;;; ([&quot;B&quot; 0.09090909090909091]
;;;  [&quot;H&quot; 0.09090909090909091]
;;;  [&quot;C&quot; 0.09090909090909091]
;;;  [&quot;E&quot; 0.09090909090909091]
;;;  [&quot;G&quot; 0.09090909090909091]
;;;  [&quot;A&quot; 0.09090909090909091]
;;;  [&quot;D&quot; 0.09090909090909091]
;;;  [&quot;J&quot; 0.09090909090909091]
;;;  [&quot;F&quot; 0.09090909090909091]
;;;  [&quot;K&quot; 0.09090909090909091]
;;;  [&quot;I&quot; 0.09090909090909091])
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(/ 1.0 11)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>0.09090909090909091</span>","value":"0.09090909090909091"}
;; <=

;; **
;;; At each time step, the computation should yield $$PR(v_i;t+1) =$$
;;; 
;;; $$\frac{1-d}{N} + d \sum_{v_j \in S(v_i)} \frac{PR (v_j; t)}{E(v_j)}$$
;; **

;; **
;;; Where:
;;; 
;;; @@v_j@@ is a member of the set of vertices @@S(v_i)@@ that link to @@v_i@@.
;;; 
;;; @@{E(v_j)}@@ is the number of outgoing edges from @@v_j@@.
;;; 
;;; @@PR (v_j; t)@@ is the PageRank of @@v_j@@ from the previous time step, @@t@@.
;;; 
;;; @@d@@ is a damping factor. A damping factor of 0.85 (85%) corresponds to a 0.15 (15%) likelihood of jumping to a random vertex, i.e. @@1-d@@. This corrects for vertices with no incoming edges ending up with a PageRank of 0. In the presence of damping, vertex A effectively links to all pages in the network even though it has no outgoing links of its own.
;;; 
;;; In each iteration of the algorithm, every vertex distributes its rank to those vertices to which it has outgoing edges. Each vertex's new PageRank will thus be based on how much rank it receives from each of the vertices that link to it, combined in a weighted average with a baseline amount of rank which each vertex gets at each time step regardless of its neighbors. Over time, this process causes the rank of each page to converge to the actual PageRank values for each page, and the sum of all rank values is always 1.
;; **

;; **
;;; After increasing the number of iterations, the algorithm quickly converges to our expected result:
;; **

;; @@
(pprint (pagerank test/example 500))
;; @@
;; ->
;;; ([&quot;B&quot; 0.3843965154422865]
;;;  [&quot;C&quot; 0.34291471887964714]
;;;  [&quot;E&quot; 0.08088569323449774]
;;;  [&quot;D&quot; 0.039087092099966095]
;;;  [&quot;F&quot; 0.039087092099966095]
;;;  [&quot;A&quot; 0.03278149315934399]
;;;  [&quot;H&quot; 0.016169479016858404]
;;;  [&quot;G&quot; 0.016169479016858404]
;;;  [&quot;J&quot; 0.016169479016858404]
;;;  [&quot;K&quot; 0.016169479016858404]
;;;  [&quot;I&quot; 0.016169479016858404])
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(compose (list-plot (map second (pagerank test/example 0))
                    :plot-range [:all [0 0.5]])

         (list-plot (map second (pagerank test/example 500))
                    :plot-range [:all [0 0.5]]
                    :color :red))
;; @@
;; =>
;;; {"type":"vega","content":{"width":400,"height":247.2187957763672,"padding":{"top":10,"left":55,"bottom":40,"right":10},"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"dfaf2410-0b5d-4285-8d00-b82ca3326217","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":[0,0.5]}],"axes":[{"type":"x","scale":"x"},{"type":"y","scale":"y"}],"data":[{"name":"dfaf2410-0b5d-4285-8d00-b82ca3326217","values":[{"x":0,"y":0.09090909090909091},{"x":1,"y":0.09090909090909091},{"x":2,"y":0.09090909090909091},{"x":3,"y":0.09090909090909091},{"x":4,"y":0.09090909090909091},{"x":5,"y":0.09090909090909091},{"x":6,"y":0.09090909090909091},{"x":7,"y":0.09090909090909091},{"x":8,"y":0.09090909090909091},{"x":9,"y":0.09090909090909091},{"x":10,"y":0.09090909090909091}]},{"name":"95daf56c-681f-4bbd-8012-a6a85b3e953c","values":[{"x":0,"y":0.3843965154422865},{"x":1,"y":0.34291471887964714},{"x":2,"y":0.08088569323449774},{"x":3,"y":0.039087092099966095},{"x":4,"y":0.039087092099966095},{"x":5,"y":0.03278149315934399},{"x":6,"y":0.016169479016858404},{"x":7,"y":0.016169479016858404},{"x":8,"y":0.016169479016858404},{"x":9,"y":0.016169479016858404},{"x":10,"y":0.016169479016858404}]}],"marks":[{"type":"symbol","from":{"data":"dfaf2410-0b5d-4285-8d00-b82ca3326217"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"fill":{"value":"steelblue"},"fillOpacity":{"value":1}},"update":{"shape":"circle","size":{"value":70},"stroke":{"value":"transparent"}},"hover":{"size":{"value":210},"stroke":{"value":"white"}}}},{"type":"symbol","from":{"data":"95daf56c-681f-4bbd-8012-a6a85b3e953c"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"fill":{"value":"red"},"fillOpacity":{"value":1}},"update":{"shape":"circle","size":{"value":70},"stroke":{"value":"transparent"}},"hover":{"size":{"value":210},"stroke":{"value":"white"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 400, :height 247.2188, :padding {:top 10, :left 55, :bottom 40, :right 10}, :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"dfaf2410-0b5d-4285-8d00-b82ca3326217\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain [0 0.5]}], :axes [{:type \"x\", :scale \"x\"} {:type \"y\", :scale \"y\"}], :data ({:name \"dfaf2410-0b5d-4285-8d00-b82ca3326217\", :values ({:x 0, :y 0.09090909090909091} {:x 1, :y 0.09090909090909091} {:x 2, :y 0.09090909090909091} {:x 3, :y 0.09090909090909091} {:x 4, :y 0.09090909090909091} {:x 5, :y 0.09090909090909091} {:x 6, :y 0.09090909090909091} {:x 7, :y 0.09090909090909091} {:x 8, :y 0.09090909090909091} {:x 9, :y 0.09090909090909091} {:x 10, :y 0.09090909090909091})} {:name \"95daf56c-681f-4bbd-8012-a6a85b3e953c\", :values ({:x 0, :y 0.3843965154422865} {:x 1, :y 0.34291471887964714} {:x 2, :y 0.08088569323449774} {:x 3, :y 0.039087092099966095} {:x 4, :y 0.039087092099966095} {:x 5, :y 0.03278149315934399} {:x 6, :y 0.016169479016858404} {:x 7, :y 0.016169479016858404} {:x 8, :y 0.016169479016858404} {:x 9, :y 0.016169479016858404} {:x 10, :y 0.016169479016858404})}), :marks ({:type \"symbol\", :from {:data \"dfaf2410-0b5d-4285-8d00-b82ca3326217\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 1}}, :update {:shape \"circle\", :size {:value 70}, :stroke {:value \"transparent\"}}, :hover {:size {:value 210}, :stroke {:value \"white\"}}}} {:type \"symbol\", :from {:data \"95daf56c-681f-4bbd-8012-a6a85b3e953c\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :fill {:value :red}, :fillOpacity {:value 1}}, :update {:shape \"circle\", :size {:value 70}, :stroke {:value \"transparent\"}}, :hover {:size {:value 210}, :stroke {:value \"white\"}}}})}}"}
;; <=

;; **
;;; ### PageRank vs. Degree Centrality
;;; 
;;; If you're new to PageRank, you may find it surprising that "C" has a higher PageRank than "E", even though there are fewer links to "C". The one link to "C" comes from an important vertex and hence is of high value.
;;; 
;;; We can use the `degrees` function in Edgewise to easily view the outdegree and indegree of each vertex:
;; **

;; @@
(pprint (degrees test/example))
;; @@
;; ->
;;; {&quot;K&quot; {:out 2, :in 0},
;;;  &quot;G&quot; {:out 1, :in 0},
;;;  &quot;J&quot; {:out 2, :in 0},
;;;  &quot;H&quot; {:out 1, :in 0},
;;;  &quot;E&quot; {:out 3, :in 6},
;;;  &quot;C&quot; {:out 1, :in 1},
;;;  &quot;F&quot; {:out 2, :in 1},
;;;  &quot;B&quot; {:out 1, :in 7},
;;;  &quot;A&quot; {:out 0, :in 1},
;;;  &quot;I&quot; {:out 2, :in 0},
;;;  &quot;D&quot; {:out 2, :in 1}}
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; This function makes it easy to compute a ranking based on degree:
;; **

;; @@
(defn degree-centrality [g direction]
  (map (fn [[k degree]] [k (degree direction)])
       (sort-by (fn [[k degree]] (degree direction)) > (degrees g))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;pagerank/degree-centrality</span>","value":"#'pagerank/degree-centrality"}
;; <=

;; **
;;; And we can now easily see the rank of E and C change:
;; **

;; @@
(pprint (degree-centrality test/example :in))
;; @@
;; ->
;;; ([&quot;B&quot; 7]
;;;  [&quot;E&quot; 6]
;;;  [&quot;C&quot; 1]
;;;  [&quot;F&quot; 1]
;;;  [&quot;A&quot; 1]
;;;  [&quot;D&quot; 1]
;;;  [&quot;K&quot; 0]
;;;  [&quot;G&quot; 0]
;;;  [&quot;J&quot; 0]
;;;  [&quot;H&quot; 0]
;;;  [&quot;I&quot; 0])
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ### The Effect of Damping
;;; 
;;; Recall that @@1-d@@ is the probability of jumping to a random vertex. If we set @@d = 1@@, we remove the effect of damping. Without damping, all rank will diffuse to vertices B and C, and all other vertices will have a PageRank of 0.
;;; 
;;; C actually becomes the most central vertex in this scenario; rank enters through B and only slowly diffuses out:
;; **

;; @@
(pprint (pagerank test/example 2001 1))
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
;;; ## Convergence
;;; 
;;; The default PageRank implementation runs until convergence, i.e. until the values stop changing beyond some small tolerance. The number of iterations taken to converge and the runtime in milliseconds is included as metadata on the result.
;;; 
;;; PageRank can terminate early with a maximum number of iterations if necessary. For this small example, rank converges very quickly after just a few iterations, although the values change until iteration 62. We can see this by changing the `max-iterations` parameter:
;; **

;; @@
(meta (pagerank test/example 100))
(meta (pagerank test/example 4))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:iteration</span>","value":":iteration"},{"type":"html","content":"<span class='clj-long'>4</span>","value":"4"}],"value":"[:iteration 4]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:runtime-millis</span>","value":":runtime-millis"},{"type":"html","content":"<span class='clj-double'>0.566293</span>","value":"0.566293"}],"value":"[:runtime-millis 0.566293]"}],"value":"{:iteration 4, :runtime-millis 0.566293}"}
;; <=

;; @@
(table-view
(map #(apply conj %1 %2) 
     (pagerank test/example 1000) 
     (pagerank test/example 4)))
;; @@
;; =>
;;; {"type":"list-like","open":"<center><table>","close":"</table></center>","separator":"\n","items":[{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;B&quot;</span>","value":"\"B\""},{"type":"html","content":"<span class='clj-double'>0.3843965154422865</span>","value":"0.3843965154422865"},{"type":"html","content":"<span class='clj-string'>&quot;C&quot;</span>","value":"\"C\""},{"type":"html","content":"<span class='clj-double'>0.3647813279471857</span>","value":"0.3647813279471857"}],"value":"[\"B\" 0.3843965154422865 \"C\" 0.3647813279471857]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;C&quot;</span>","value":"\"C\""},{"type":"html","content":"<span class='clj-double'>0.34291471887964714</span>","value":"0.34291471887964714"},{"type":"html","content":"<span class='clj-string'>&quot;B&quot;</span>","value":"\"B\""},{"type":"html","content":"<span class='clj-double'>0.3136614228599689</span>","value":"0.3136614228599689"}],"value":"[\"C\" 0.34291471887964714 \"B\" 0.3136614228599689]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;E&quot;</span>","value":"\"E\""},{"type":"html","content":"<span class='clj-double'>0.08088569323449774</span>","value":"0.08088569323449774"},{"type":"html","content":"<span class='clj-string'>&quot;E&quot;</span>","value":"\"E\""},{"type":"html","content":"<span class='clj-double'>0.08688750795016452</span>","value":"0.08688750795016452"}],"value":"[\"E\" 0.08088569323449774 \"E\" 0.08688750795016452]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;D&quot;</span>","value":"\"D\""},{"type":"html","content":"<span class='clj-double'>0.039087092099966095</span>","value":"0.039087092099966095"},{"type":"html","content":"<span class='clj-string'>&quot;D&quot;</span>","value":"\"D\""},{"type":"html","content":"<span class='clj-double'>0.05207865887052515</span>","value":"0.05207865887052515"}],"value":"[\"D\" 0.039087092099966095 \"D\" 0.05207865887052515]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;F&quot;</span>","value":"\"F\""},{"type":"html","content":"<span class='clj-double'>0.039087092099966095</span>","value":"0.039087092099966095"},{"type":"html","content":"<span class='clj-string'>&quot;F&quot;</span>","value":"\"F\""},{"type":"html","content":"<span class='clj-double'>0.05207865887052515</span>","value":"0.05207865887052515"}],"value":"[\"F\" 0.039087092099966095 \"F\" 0.05207865887052515]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;A&quot;</span>","value":"\"A\""},{"type":"html","content":"<span class='clj-double'>0.03278149315934399</span>","value":"0.03278149315934399"},{"type":"html","content":"<span class='clj-string'>&quot;A&quot;</span>","value":"\"A\""},{"type":"html","content":"<span class='clj-double'>0.03759596479509769</span>","value":"0.03759596479509769"}],"value":"[\"A\" 0.03278149315934399 \"A\" 0.03759596479509769]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;H&quot;</span>","value":"\"H\""},{"type":"html","content":"<span class='clj-double'>0.016169479016858404</span>","value":"0.016169479016858404"},{"type":"html","content":"<span class='clj-string'>&quot;H&quot;</span>","value":"\"H\""},{"type":"html","content":"<span class='clj-double'>0.018583291741306587</span>","value":"0.018583291741306587"}],"value":"[\"H\" 0.016169479016858404 \"H\" 0.018583291741306587]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;G&quot;</span>","value":"\"G\""},{"type":"html","content":"<span class='clj-double'>0.016169479016858404</span>","value":"0.016169479016858404"},{"type":"html","content":"<span class='clj-string'>&quot;G&quot;</span>","value":"\"G\""},{"type":"html","content":"<span class='clj-double'>0.018583291741306587</span>","value":"0.018583291741306587"}],"value":"[\"G\" 0.016169479016858404 \"G\" 0.018583291741306587]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;J&quot;</span>","value":"\"J\""},{"type":"html","content":"<span class='clj-double'>0.016169479016858404</span>","value":"0.016169479016858404"},{"type":"html","content":"<span class='clj-string'>&quot;J&quot;</span>","value":"\"J\""},{"type":"html","content":"<span class='clj-double'>0.018583291741306587</span>","value":"0.018583291741306587"}],"value":"[\"J\" 0.016169479016858404 \"J\" 0.018583291741306587]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;K&quot;</span>","value":"\"K\""},{"type":"html","content":"<span class='clj-double'>0.016169479016858404</span>","value":"0.016169479016858404"},{"type":"html","content":"<span class='clj-string'>&quot;K&quot;</span>","value":"\"K\""},{"type":"html","content":"<span class='clj-double'>0.018583291741306587</span>","value":"0.018583291741306587"}],"value":"[\"K\" 0.016169479016858404 \"K\" 0.018583291741306587]"},{"type":"list-like","open":"<tr><td>","close":"</td></tr>","separator":"</td><td>","items":[{"type":"html","content":"<span class='clj-string'>&quot;I&quot;</span>","value":"\"I\""},{"type":"html","content":"<span class='clj-double'>0.016169479016858404</span>","value":"0.016169479016858404"},{"type":"html","content":"<span class='clj-string'>&quot;I&quot;</span>","value":"\"I\""},{"type":"html","content":"<span class='clj-double'>0.018583291741306587</span>","value":"0.018583291741306587"}],"value":"[\"I\" 0.016169479016858404 \"I\" 0.018583291741306587]"}],"value":"#gorilla_repl.table.TableView{:contents ([\"B\" 0.3843965154422865 \"C\" 0.3647813279471857] [\"C\" 0.34291471887964714 \"B\" 0.3136614228599689] [\"E\" 0.08088569323449774 \"E\" 0.08688750795016452] [\"D\" 0.039087092099966095 \"D\" 0.05207865887052515] [\"F\" 0.039087092099966095 \"F\" 0.05207865887052515] [\"A\" 0.03278149315934399 \"A\" 0.03759596479509769] [\"H\" 0.016169479016858404 \"H\" 0.018583291741306587] [\"G\" 0.016169479016858404 \"G\" 0.018583291741306587] [\"J\" 0.016169479016858404 \"J\" 0.018583291741306587] [\"K\" 0.016169479016858404 \"K\" 0.018583291741306587] [\"I\" 0.016169479016858404 \"I\" 0.018583291741306587]), :opts nil}"}
;; <=

;; **
;;; <div style="color: white;">-</div>
;;; 
;;; <script type="text/javascript">
;;; function resize() {
;;;   var height = document.getElementsByTagName("html")[0].scrollHeight;
;;;   window.parent.postMessage(["setHeight", height], "*");
;;; }
;;; 
;;; resize();
;;; </script>
;; **
