(ns lectures.core
  (:use [lectures.compile :only (recompile recompile-all)])
  (gen-class :main true))

(defn -main
  ([]
   (recompile-all)
   (shutdown-agents))
  ([lecture]
   (recompile lecture)
   (shutdown-agents)))
