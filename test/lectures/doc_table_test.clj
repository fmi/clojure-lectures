(ns lectures.doc-table-test
  (:use clojure.test
        lectures.doc-table))

(deftest doc-table-test
  (testing "Collects all symbol documentation"
    (with-doc-table
      (gather-docs "(inc 1)")
      (is (= {'inc (get-doc 'inc)} (get-doc-table)))))
  (testing "Collects multiple forms"
    (with-doc-table
      (gather-docs "(inc 1) (dec 1)")
      (is ((get-doc-table) 'inc))
      (is ((get-doc-table) 'dec))))
  (testing "Does not get confused by a trailing comment"
    (with-doc-table
      (gather-docs "(inc 1) ;")
      (is ((get-doc-table) 'inc))))
  (testing "Does not include nil for doc-less symbols"
    (with-doc-table
      (gather-docs "(def answer 42)")
      (is ((get-doc-table) 'def))
      (is (not ((get-doc-table) 'answer)))))
  (testing "Ignores broken code"
    (with-doc-table
      (gather-docs "(inc")
      (is (= {} (get-doc-table)))))
  (testing "Does not break with classes that do not exist"
    (with-doc-table
      (gather-docs "example.R")
      (is (= {} (get-doc-table)))))
  (testing "Works when no doc-table set"
    (is (not (thread-bound? #'*doc-table*)))
    (gather-docs "(inc 1)")))

(run-tests)
