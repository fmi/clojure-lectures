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
               (def placeholder-outputs (atom {}))
               (defmacro store-result [number & body]
                 `(try
                    (let [output# (new java.io.StringWriter)]
                      (binding [*out* output#]
                        (let [value# (do ~@body)]

                          (swap! ~'lectures.sandbox/placeholder-values assoc ~number value#)
                          (swap! ~'lectures.sandbox/placeholder-outputs
                                 assoc ~number (str output#))
                          value#)))
                    (catch Throwable e#
                      (swap! ~'lectures.sandbox/placeholder-values assoc ~number e#)
                      nil)))))
      (eval (read-string code)))
    (let [result (eval '@lectures.sandbox/placeholder-values)
          outputs (eval '@lectures.sandbox/placeholder-outputs)]
      (remove-ns 'lectures.sandbox)
      {:values result, :outputs outputs})))

(defn- placeholder->str
  [value]
  (condp instance? value
    Throwable (str (-> value class .getName) ": " (.getMessage value))
    (pr-str value)))

(defn- prettify-output
  [output]
  (->> output
       str/split-lines
       (map (partial str "; "))
       (str/join "\n")
       (str ";; OUTPUT:\n")))


(defn- replace-placeholders
  [code placeholders outputs]
  (str/replace code #"(?m)\{(\d+)\}$"
    (fn [[_ match]]
      (let [id (Integer. match)
            value-string (-> id
                             placeholders
                             placeholder->str)
            output (outputs id)]
        (if (not-empty output)
          (str value-string "\n" (prettify-output output))
          value-string)))))

(defn annotate
  [code]
  (let [code (assign-placeholders code)
        {:keys [values outputs]} (eval-placeholders code)]
    (replace-placeholders code values outputs)))
