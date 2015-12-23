(ns edgewise.util)

;; select-keys is a filter function that returns a map
;; for the purposes of graph traversal, we want to allow duplicate keys to return multiple instances
(defn select-vals [map keyseq]
  (loop [ret [] keys (seq keyseq)]
    (if keys
      (let [entry (find map (first keys))]
        (recur
         (if entry
           (conj ret (second entry))
           ret)
         (next keys)))
      ret)))

(defn nearly [expected actual]
  (let [epsilon 0.0001]
    (< (Math/abs (- expected actual)) epsilon)))
