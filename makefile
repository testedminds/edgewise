SHELL := /usr/bin/env bash

counter = printf "\t$1: `find $1 -name '*.clj' | xargs sed '/^\s*;;/d;/^\s*$$/d' | wc -l`\n"

.SILENT: line-count
line-count:
	echo "non-comment lines in:"
	$(call counter,./src)
	$(call counter,./test)

