(ns wewe.weather
  (:require [config.core :refer [env]]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(def api-key (:openweather-api-key env))

(defn get-weather [id]
  (log/info "fetching weather for" id)
  (json/parse-string
    (slurp (str "http://api.openweathermap.org/data/2.5/weather?id=" id "&APPID=" api-key "&units=metric"))
    true))

(comment
  (get-weather 734077))


