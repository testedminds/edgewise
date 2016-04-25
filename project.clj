(defproject edgewise "0.3.1-SNAPSHOT"
  :description "Edgewise is a simple library for representing and computing on networks (graphs) in Clojure."
  :url "https://github.com/testedminds/edgewise"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :pedantic? :warn
  :profiles {:dev {:plugins [[lein-gorilla "0.3.6"]]}
             :test {:dependencies [[gorilla-repl "0.3.6"]]}}
  :test-paths ["test" "doc"]
  :test-selectors {:default (fn [m] (not (or (:perf m) (:doc m))))
                   :perf :perf
                   :doc :doc})
