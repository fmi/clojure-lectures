(ns lectures.server
  (:use compojure.core
        [lectures.compile :only (compiled)])
  (:require [compojure.route :as route]))

(defroutes lecture-routes
  (GET "/:lecture" [lecture] (compiled lecture))
  (route/resources "/" {:root nil})
  (route/not-found "This needs <strong>way</strong> more work ;)"))

(def app (routes lecture-routes))
