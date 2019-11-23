(ns wewe.db
  (:require
    [config.core :refer [env]]
    [taoensso.carmine :as car :refer (wcar)]))

(def redis-url (or (:redis-url env) "redis://localhost:6379"))
(def server1-conn {:pool {} :spec {:uri redis-url}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn insert-city [id lat lon]
  (wcar*
    (car/geoadd "wewe:city" lon lat id)))

(defn cities-in-radius
  "Returns all the cities in the radius (km) sorted
  by dist asc in {:id id :dist dist} format"
  [lat lon radius]
  (->>
    (wcar*
      (car/georadius "wewe:city" lon lat radius "km" "withdist"))
    (map (fn [[id dist]] {:id (Integer. id) :dist (Double. dist)}))
    (sort-by :dist)))

(comment
  (wcar*
    (car/del "wewe:city")))

#_(cities-in-radius 40.64 22.944 10)
