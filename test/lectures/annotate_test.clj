(ns lectures.annotate-test
  (:use clojure.test
        lectures.annotate))

(deftest annotate-test
  (are [input expected] (= (annotate input) expected)
       ; Same line annotation
       "(+ 1 1) ; =>"
       "(+ 1 1) ; 2"

       ; Refering to a symbol in the namespace
       "(def x 2)
        (inc x) ; =>"
       "(def x 2)
        (inc x) ; 3"

       ; Each time the namespace is cleared
       "(defonce y 1)
        y ; =>"
       "(defonce y 1)
        y ; 1"
       "(defonce y 2)
        y ; =>"
       "(defonce y 2)
        y ; 2"

       ; Exceptions
       "(throw (RuntimeException. \"Message\")) ; =>"
       "(throw (RuntimeException. \"Message\")) ; java.lang.RuntimeException: Message"

       ; Previous line annotation
       "(map inc [1 2 3])
        ; =>"
       "(map inc [1 2 3])
        ; (2 3 4)"

       ; Namespaces
       "(in-ns 'foo)"
       "(in-ns 'foo)"))
