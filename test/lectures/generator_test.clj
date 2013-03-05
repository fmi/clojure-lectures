(ns lectures.generator-test
  (:use clojure.test
        lectures.generator))

(deftest generator-test
  (are [input expected] (= (generate input) expected)
       [:code "foo"]   [:code "foo"]
       [:code "x < y"] [:code "x &lt; y"]

       [:bold "foo"]   [:strong "foo"]
       [:bold "<img>"] [:strong "<img>"]

       [:paragraph "Foo"]               [:p "Foo"]
       [:paragraph "Foo" [:bold "Bar"]] [:p "Foo" [:strong "Bar"]]

       [:block :code "(clojure code)"]     [:pre {:class "brush: clojure"} "(clojure code)"]
       [:block :annotate "(+ 1 2) ; =>"]   [:pre {:class "brush: clojure"} "(+ 1 2) ; 3"]

       [:bullet-list [:incremental "Foo"] [:incremental "Bar"]]
       [:ul [:li {:class "action"} "Foo"] [:li {:class "action"} "Bar"]]

       [:bullet-list [:incremental "Foo"] [:static "Bar"]]
       [:ul [:li {:class "action"} "Foo"] [:li "Bar"]]

       [:bullet-list [:static "Foo" [:bold "Bar"] "Baz"]]
       [:ul [:li "Foo" [:strong "Bar"] "Baz"]]

       [:slide ["Title"] ["Subtitle"]
        [:paragraph "Paragraph"]]
       [:section {:class "slide"}
        [:hgroup
         [:h1 "Title"]
         [:h2 "Subtitle"]]
        [:p "Paragraph"]]

       [:slide [[:code "Clojure"] " rocks"] nil
        [:paragraph "It is true!"]]
       [:section {:class "slide"}
        [:hgroup [:h1 [:code "Clojure"] " rocks"]]
        [:p "It is true!"]]))

(run-tests)
