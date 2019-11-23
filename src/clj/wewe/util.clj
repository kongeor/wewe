(ns wewe.util
  (:require [cheshire.core :as json]
            [wewe.db :as db]))


(def gr-cities
  (->> (json/parse-string (slurp "/home/kostas/Downloads/city.list.json") true)
       (filter #(= (:country %) "GR"))))

(filter #(= "Thessaloniki" (% :name)) gr-cities)

(doseq [city gr-cities]
  (println city)
  (db/insert-city (:id city) (-> city :coord :lat) (-> city :coord :lon)))
