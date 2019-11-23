(ns wewe.util
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [wewe.db :as db]))

(comment
  (def gr-cities
    (->> (json/parse-string (slurp "/home/kostas/Downloads/city.list.json") true)
      (filter #(= (:country %) "GR"))))

  (spit "/tmp/data.edn" (with-out-str (pr gr-cities)))

  (read-string (slurp (io/resource "cities_gr.edn")))

  (filter #(= "Thessaloniki" (% :name)) gr-cities)

  (doseq [city gr-cities]
    (println city)
    (db/insert-city (:id city) (-> city :coord :lat) (-> city :coord :lon))))

(defn add-gr-cities-from-resource []
  (let [gr-cities (read-string (slurp (io/resource "cities_gr.edn")))]
    (doseq [city gr-cities]
      (db/insert-city (:id city) (-> city :coord :lat) (-> city :coord :lon)))
    (log/info "added" (count gr-cities) "gr cities")))


#_(add-gr-cities-from-resource)
