(ns lectures.doc-table
  (:require [clojure.string :as str])
  (:use [clojure.walk :only (postwalk)]))

(def ^:dynamic *doc-table*)

(defn make-doc-table []
  (atom {}))

(defn- read-code
  [code]
  (try
    (read-string (str "(\n" code "\n)"))
    (catch RuntimeException e nil)))

(defn get-doc
  "Retrieves the documentation of a symbol. Returns nil if it
   cannot be found. Uses the doc macro."
  [sym]
  (try
    (let [doc-str (with-out-str (eval (list 'clojure.repl/doc sym)))]
      (when (not= doc-str "")
        (str/replace doc-str #"^-+\n" "")))
    (catch RuntimeException e nil)
    (catch ClassNotFoundException e nil)))

(defn- add-to-table
  [thing]
  (when (and (symbol? thing)
             (not (find @*doc-table* thing)))
    (swap! *doc-table* assoc thing (get-doc thing))))

(defmacro with-doc-table
  "Executes body with a set doc table."
  [& body]
  `(binding [*doc-table* (make-doc-table)]
     ~@body))

(defn get-doc-table
  "Returns a map with all the doc syms and their documentation."
  []
  (when (thread-bound? #'*doc-table*)
    (into {} (filter val @*doc-table*))))

(defn gather-docs
  "Collects the documentation for all symbols in code and puts it in *doc-table*."
  [code]
  (when (thread-bound? #'*doc-table*)
    (if-let [sexp (read-code code)]
      (postwalk add-to-table sexp))))
