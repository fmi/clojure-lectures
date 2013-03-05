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
                 (str/replace #"(?m)^(.*)\s*; \{(\d+)\}$" "(store-result $2 $1)"))]
    (binding [*ns* (create-ns 'lectures.sandbox)]
      (eval '(do
               (ns lectures.sandbox)
               (def placeholder-values (atom {}))
               (defn- store-result [number value]
                 (swap! placeholder-values assoc number value)
                 value)))
      (eval (read-string code))
      (let [result (eval '@placeholder-values)]
        (remove-ns 'lectures.sandbox)
        result))))

(defn- replace-placeholders
  [code placeholders]
  (str/replace code #"(?m)\{(\d+)\}$" #(-> % second Integer. placeholders pr-str)))

(defn annotate
  [code]
  (let [code (assign-placeholders code)
        values (eval-placeholders code)]
    (replace-placeholders code values)))
