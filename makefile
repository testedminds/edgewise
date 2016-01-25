SHELL := /usr/bin/env bash

.SILENT: line-count
count = `find $1 -name '*.clj' | xargs sed '/^\s*;;/d;/^\s*$$/d' | wc -l`
line-count:
	echo "NON-COMMENT LINES:"
	(printf "SRC TEST\n" ; \
	 printf "$(call count,./src) " ; \
	 printf "$(call count,./test)\n") \
	 | column -t

.PHONY: doc test
doc:
	lein test :doc

test:
	@lein test edgewise.degree-test edgewise.edgelist.reader-test edgewise.edgelist.writer-test edgewise.graph-test edgewise.loops-test edgewise.pagerank-test edgewise.tgf.reader-test edgewise.tgf.writer-test

grepl:
	open http://127.0.0.1:`cat .gorilla-port`/worksheet.html?filename=doc/pagerank.clj
