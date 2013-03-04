(ns lectures.generator
  (:use hiccup.core))

(defn- generate-dispatch [ast]
  (cond (string? ast) :text
        (keyword? (first ast)) (first ast)
        :else :text-line))

(defmulti generate
  #'generate-dispatch)

(defmethod generate :text [string]
  string)

(defmethod generate :code [[_ code]]
  [:code (h code)])

(defmethod generate :bold [[_ text]]
  [:strong text])

(defmethod generate :text-line [chunks]
  (mapv generate chunks))

(defmethod generate :paragraph [[_ & chunks]]
  `[:p ~@(generate chunks)])

(defmethod generate :block [[_ kind code]]
  [:pre code])

(defmethod generate :bullet-list [[_ & items]]
  `[:ul ~@(map generate items)])

(defmethod generate :incremental [[_ & chunks]]
  `[:li {:class "action"} ~@(generate chunks)])

(defmethod generate :static [[_ & chunks]]
  `[:li ~@(generate chunks)])

(defmethod generate :slide [[_ title subtitle & chunks]]
  `[:section {:class "slide"}
    [:hgroup [:h1 ~@(generate title)]
             ~@(when subtitle `[[:h2 ~@(generate subtitle)]])]
    ~@(map generate chunks)])
