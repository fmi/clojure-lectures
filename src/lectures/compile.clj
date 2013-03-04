(ns lectures.compile
  (:require [clojure.java.io :as io]
            [clj-yaml.core :as yaml]
            [me.raynes.fs :as fs])
  (:use [lectures.parser :only (parse)]
        [lectures.generator :only (generate-lecture)]))

(def target-dir
  "The directory where all the lectures will be generated, relative
   to the current directory when ran."
  "target/lectures")

(defn- target-file-name
  "Builds a filename in the target directory."
  [filename]
  (str target-dir "/" filename))

(defn- read-lectures-index
  "Reads the lecture index."
  []
  (-> "lectures/index.yml"
      io/file
      slurp
      yaml/parse-string))

(defn- compile-lecture
  "Compiles a single lecture in the target directory. Takes the lecture
   data as a map."
  [{:keys [title date slug]}]
  (let [source-file  (str "lectures/" slug ".lecture")
        target-file  (target-file-name (str slug ".html"))
        lecture-ast  (-> source-file slurp parse)
        lecture-html (generate-lecture title date lecture-ast)]
    (spit target-file lecture-html)
    (fs/copy-dir (str "lectures/" slug) target-dir)))

(defn- prepare-target-dir
  "Clobbers the target directory, recreates it and copies the static files
   into it."
  []
  (fs/delete-dir target-dir)
  (fs/mkdir target-dir)

  (fs/copy-dir "resources/images"   target-dir)
  (fs/copy-dir "resources/css"      target-dir)
  (fs/copy-dir "resources/js"       target-dir)
  (fs/copy     "lectures/index.yml" (target-file-name "index.yml")))

(defn recompile
  "Recompiles a single lecture."
  [number]
  (let [index (read-lectures-index)
        lecture-key (-> number str keyword)
        lecture (lecture-key index)]
    (compile-lecture lecture)))

(defn recompile-all
  "Recompiles all the lectures."
  []
  (prepare-target-dir)
  (doseq [lecture (vals (read-lectures-index))
          :when (:slug lecture)]
    (compile-lecture lecture)))
