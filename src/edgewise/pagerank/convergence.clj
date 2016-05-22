(ns edgewise.pagerank.convergence)

(defn converge
  "Returns the result of iterating (f x) while pred is true."
  [f x pred]
  (ffirst
   (drop-while
    pred
    (partition 2 (iterate f x)))))

(defn converging?
  "Returns true if every member of seq a [[ak av]] matches seq b [[bk bv]] within the specified tolerance."
  [a b]
  (not-every?
    true?
    (map (fn [x y]
           (and (= (first x) (first y))
                (<= (Math/abs (- (second x) (second y))) 0.00001))) a b)))
