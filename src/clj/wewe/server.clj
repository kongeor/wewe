(ns wewe.server
  (:require [wewe.handler :refer [handler]]
            [wewe.util :as util]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (util/add-gr-cities-from-resource)
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-jetty handler {:port port :join? false})))

(comment
  (def server (run-jetty handler {:port 3000 :join? false}))
  (.stop server))
