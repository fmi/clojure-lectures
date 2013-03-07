(ns lectures.parser-test
  (:use clojure.test
        lectures.parser)
  (:require [clojure.string :as str]))

(defmacro expect
  [parser & body]
  `(are [input# output#] (= (parse ~parser input#) output#)
        ~@body))

(defn- with-margin
  "Removes all leading whitespace followed by a pipe. This is useful to create
   multiline-strings with the Clojure literals that are don't start in the beginning
   of their source file."
  [string]
  (->> string
       str/split-lines
       (map #(str/replace % #"^\s*\|" ""))
       (str/join "\n")))

(deftest parser-test
  (testing "Inline code"
    (expect inline-code
            "`foo`"   [:code "foo"]
            "`x * y`" [:code "x * y"]))
  (testing "Bold text"
    (expect bold
            "*foo*"      [:bold "foo"]))
  (testing "Links"
    (expect link
            "[Clojure](http://clojure.org/)" [:link "Clojure" "http://clojure.org/"]
            "[http://clojure.org/]"          [:link "http://clojure.org/" "http://clojure.org/"]
            "[gh:fmi/evans]"                 [:link :github "fmi/evans"]))
  (testing "Line of text"
    (expect text-line
            "Text"                  ["Text"]
            "*Bold* text"           [[:bold "Bold"] " text"]
            "Unmatched *asterix"    ["Unmatched *asterix"]
            "Text with `code`"      ["Text with " [:code "code"]]
            "Unmatched `backtick"   ["Unmatched `backtick"]
            "Ending with newline\n" ["Ending with newline"]
            "With [gh:fmi/clojure]" ["With " [:link :github "fmi/clojure"]]
            "With [link](/target)"  ["With " [:link "link" "/target"]]))
  (testing "Bullet list"
    (expect bullet-list
            (with-margin "|* First
                          |* Second")
            [:bullet-list
             [:incremental "First"]
             [:incremental "Second"]]

            (with-margin "|+ First
                          |+ Second")
            [:bullet-list
             [:static "First"]
             [:static "Second"]]

            (with-margin "|+ First
                          |* Second")
            [:bullet-list
             [:static "First"]
             [:incremental "Second"]]))
  (testing "Raw HTML"
    (expect raw-html
            (with-margin "|{{{
                          |<strong>{}</strong>
                          |}}}")
            [:raw-html "\n<strong>{}</strong>\n"]

            "{{{<img />}}}"
            [:raw-html "<img />"]))
  (testing "Code blocks"
    (expect code-block
            (with-margin "|:code
                          |  (defn increment [x]
                          |    (+ x 1))")
            [:block :code
             (with-margin "|(defn increment [x]
                           |  (+ x 1))")]

            (with-margin "|:code
                          |  (foo
                          |
                          |  bar)")
            [:block :code
             (with-margin "|(foo
                           |
                           |bar)")]

            (with-margin "|:code
                          |  trailing newlines
                          |
                          |")
            [:block :code "trailing newlines"]

            (with-margin "|:annotate
                          |  (def answer 42)")
            [:block :annotate "(def answer 42)"]))
  (testing "Slide"
    (expect slide
            (with-margin "|= Title
                          |== Subtitle
                          |
                          |First line
                          |Second line")
            [:slide ["Title"] ["Subtitle"]
             [:paragraph "First line"]
             [:paragraph "Second line"]]

            (with-margin "|= Slide with a list
                          |
                          |This is a list:
                          |* First item
                          |* Second item")
            [:slide ["Slide with a list"] nil
             [:paragraph "This is a list:"]
             [:bullet-list
              [:incremental "First item"]
              [:incremental "Second item"]]]

            (with-margin "|= Slide with code
                          |
                          |This is how to define increment:
                          |
                          |:code
                          |  (defn increment [x]
                          |    (+ x 1))
                          |
                          |After the code")
            [:slide ["Slide with code"] nil
             [:paragraph "This is how to define increment:"]
             [:block :code "(defn increment [x]\n  (+ x 1))"]
             [:paragraph "After the code"]]

            (with-margin "|= Slide with raw HTML
                          |
                          |{{{
                          |<img />
                          |}}}
                          |
                          |After the code")
            [:slide ["Slide with raw HTML"] nil
             [:raw-html "\n<img />\n"]
             [:paragraph "After the code"]]))
  (testing "Presentation"
    (expect presentation
            (with-margin "|= First slide
                          |
                          |First line
                          |
                          |= Second slide
                          |
                          |Second line")
            [:presentation
             [:slide ["First slide"] nil
              [:paragraph "First line"]]
             [:slide ["Second slide"] nil
              [:paragraph "Second line"]]])))
