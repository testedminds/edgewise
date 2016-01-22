# Edgewise

Edgewise is a simple tool and library for representing and computing on networks (graphs) in Clojure.

At a high-level, edgewise fits into preliminary network exploratory data analysis by facilitating
easy translation of an adjacency list into a format that a network visualization tool like yEd or Gephi
can read.

```
raw data -> edge list
edge list -> edgewise
edgewise -> TGF
edgewise -> d3json
edgewise -> dot
edgewise -> graphml
edgewise -> ...
```

## Usage

• [Basic traversals and PageRank](http://viewer.gorilla-repl.org/view.html?source=github&user=bobbyno&repo=edgewise&path=test/edgewise/pagerank_test.clj)


## License

Copyright © Bobby Norton and Tested Minds, LLC.

Released under the [Apache License, Version 2.0](./LICENSE.txt)
