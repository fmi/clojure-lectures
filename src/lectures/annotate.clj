(ns lectures.annotate
  (:require [clojure.string :as str]))

(def ^:private placeholder-index (atom 0))
(defn- gen-placeholder
  []
  (swap! placeholder-index inc))

(defn- assign-placeholders
  [code]
  (str/replace code #"(?m); =>$" (fn [_] (str "; {" (gen-placeholder) "}"))))

(defn- eval-placeholders
  "Evaluates code (passed as a string) and fetches all the "
  [code]
  (let [code (-> (str "(do\n" code "\n)")
                 (str/replace #"\n(; \{(\d+)\})$" "$1")
                 (str/replace #"(?m)^(.*)\s*; \{(\d+)\}$" "(lectures.sandbox/store-result $2 $1)"))]
    (binding [*ns* (create-ns 'lectures.sandbox)]
      (eval '(do
               (ns lectures.sandbox)
               (def placeholder-values (atom {}))
               (defmacro store-result [number & body]
                 `(try
                    (let [value# (do ~@body)]
                      (swap! ~'lectures.sandbox/placeholder-values assoc ~number value#)
                      value#)
                    (catch Throwable e#
                      (swap! ~'lectures.sandbox/placeholder-values assoc ~number e#)
                      nil)))))
      (eval (read-string code)))
    (let [result (eval '@lectures.sandbox/placeholder-values)]
      (remove-ns 'lectures.sandbox)
      result)))

(defn- placeholder->str
  [value]
  (condp instance? value
    Throwable (str (-> value class .getName) ": " (.getMessage value))
    (pr-str value)))

(defn- replace-placeholders
  [code placeholders]
  (str/replace code #"(?m)\{(\d+)\}$" #(-> % second Integer. placeholders placeholder->str)))

(defn annotate
  [code]
  (let [code (assign-placeholders code)
        values (eval-placeholders code)]
    (replace-placeholders code values)))
