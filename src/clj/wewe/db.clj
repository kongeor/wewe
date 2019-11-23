(ns wewe.db
  (:require
    [config.core :refer [env]]
    [taoensso.carmine :as car :refer (wcar)]
    [clojure.tools.logging :as log]))

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

(defn get-weather [id]
  (wcar*
    (car/get (str "wewe:weather:" id))))

(defn set-weather
  "Set weather info for a given key
   which will be cached for 10 mins"
  [id weather]
  (let [k (str "wewe:weather:" id)]
    (wcar*
      (car/set k weather)
      (car/expire k 600))))

(comment
  #_(set-weather 123 "foo")
  (get-weather 734077))

(comment
  (wcar*
    (car/ttl "wewe:weather:734077"))
  (wcar*
    (car/keys "*")))
