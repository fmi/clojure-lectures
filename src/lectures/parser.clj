(ns lectures.parser
  (:use (blancas.kern [core :exclude (parse) :as kern]))
  (:require [clojure.string :as str]))

(defn- chars-excluding
  "Matches any number characters that are not given as arguments and are
   on the same line. Returns them as a string."
  [& excluded]
  (-> excluded
      set
      (conj \newline)
      complement
      satisfy
      many
      <+>))

(defn- surrouned-by
  "Matches any number of non-newline characters, surrounded by delim,
   and returns them in a vector, tagged with tag."
  [delim tag]
  (bind [text (between (sym* delim) (sym* delim)
                       (chars-excluding delim))]
    (return [tag text])))

(def non-newline
  "Matches a non-newline character."
  (satisfy (complement #{\return \newline})))

(def inline-code
  "Matches `inline code`."
  (surrouned-by \` :code))

(def bold
  "Matches *bold text*."
  (surrouned-by \* :bold))

(def text-line
  "Matches a single line of text that can include inline declarations like
   code blocks or bold text. Returns a vector of chunks."
  (bind [
         parsed (many (<|> (<:> inline-code)
                           (<:> bold)
                           non-newline))
         _ (optional new-line*)]
    (loop [result []
           input parsed]
      (cond (empty? input) (return result)
            (char? (first input)) (recur (->> input
                                              (take-while char?)
                                              char-array
                                              String.
                                              (conj result))
                                         (drop-while char? input))
            :else (recur (conj result (first input))
                         (rest input))))))

(def bullet-list
  "Matches a bullet list. List items can be declared with * for incremental
   items and + for static items."
  (bind [lines (many1 (<*> (<|> (sym* \*) (sym* \+))
                           (skip-ws text-line)))]
    (let [item-tag {\* :incremental, \+ :static}
          items (vec (for [[sym line] lines]
                       (into [(item-tag sym)] line)))]
      (return (->> items (cons :bullet-list) vec)))))

(def code-block
  "Matches a block of code."
  (bind [lines (>> (token* ":code")
                   new-line*
                   (end-by (<|> eof new-line*)
                           (<|> (>> (token* "  ") (<+> (many non-newline)))
                                (skip (not-followed-by eof) (many space)))))]
    (return [:block :clojure (str/join "\n" lines)])))

(def slide-chunk
  "Matches a chunk in a slide."
  (<|> code-block
       bullet-list
       (>>= text-line #(return (into [:paragraph] %)))))

(def slide
  "Matches a slide. It includes a title, followed by any number of slide
   chunks."
  (bind [title (>> (sym* \=) (skip-ws text-line))
         chunks (many (>> (not-followed-by (<|> eof (sym* \=)))
                          slide-chunk))]
    (return (into [:slide title] (remove #{[:paragraph]} chunks)))))

(def presentation
  "Matches a full presentation."
  (bind [slides (many slide)]
    (return (into [:presentation] slides))))

(defn parse
  ([input] (parse presentation input))
  ([parser input] (->> input (kern/parse parser) :value)))
