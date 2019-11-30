(ns wewe.db
  (:require
    [config.core :refer [env]]
    [taoensso.carmine :as car :refer (wcar)]
    [clojure.tools.logging :as log]))

;; TODO
(defn str->double [s]
  (when s
    (Double. s)))

(defn str->int [s]
  (when s
    (Integer. s)))

(def redis-url (or (:redis-url env) "redis://localhost:6379"))
(def server1-conn {:pool {} :spec {:uri redis-url}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn insert-city [id lat lon]
  (wcar*
    (car/geoadd "wewe:city" lon lat id)))

(defn insert-city-data [id name lat lon]
  (wcar*
    (car/redis-call [:FT.ADD "wewe:city:names" id "1.0"
                     "FIELDS"
                     "name" name
                     "coords" (str lon " " lat)])))

(defn delete-city-data [id]
  (wcar*
    (car/redis-call [:FT.DEL "wewe:city:names" id])))

(defn search-city-data [name & {:keys [lat lon radius] :or {radius 100}}]
  (let [base-q [:FT.SEARCH "wewe:city:names" name]
        geo-q ["GEOFILTER" "coords" lon lat radius "km"]
        q (concat base-q (if (and lat lon) geo-q []))
        [cnt & data] (wcar* (car/redis-call q))]
    (map (fn [e]
           (println e)
           (let [coords (-> e
                          second
                          (nth 3)
                          (clojure.string/split #" "))]
             (println e)
             {:id     (str->int (first e))
              :name   (-> e second second)
              :coords {:lat (-> coords first str->double)
                       :lon (-> coords second str->double)}}))
      (partition 2 data))))

(comment
  #_(insert-city-data 2 "bar" 41.64 22.944)
  #_(delete-city-data 1)
  (search-city-data "Syk*" :lat 40.64 :lon 22.944)
  (search-city-data "th*"))


(defn create-city-index []
  (wcar*
    (car/redis-call [:FT.CREATE "wewe:city:names" "SCHEMA"
                     "name" "TEXT"
                     "coords" "GEO"])))

(defn drop-city-index []
  (wcar*
    (car/redis-call [:FT.DROP "wewe:city:names"])))

(comment
  (create-city-index)
  (drop-city-index))

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

(defn get-city-suggestions [text]
  (let [data
        (wcar*
          (car/redis-call [:FT.SUGGET "wewe:city:names" text "FUZZY" "WITHSCORE"]))]
    (map (partial zipmap [:city :score]) (partition 2 data))))

(comment
  (add-city-suggestion "yolo")
  (get-city-suggestions "yo"))

(defn add-city-suggestion [text]
  (wcar*
    (car/redis-call [:FT.SUGADD "wewe:city:names" text 1.0])))

(comment
  (wcar*
    (car/ttl "wewe:weather:734077"))
  (wcar*
    (car/keys "*"))
  (wcar*
    (car/redis-call [:ft.sugget "cities" "th" "FUZZY" "WITHSCORE"])))

