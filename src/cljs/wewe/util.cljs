(ns wewe.util
  (:require [re-frame.core :as re-frame]
            [wewe.events :as events]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            ))


(defn pos-error []
  (println "boom!"))

(defn pos-success [pos]
  (let [position {:lat (.. pos -coords -latitude)
                  :lon (.. pos -coords -longitude)}]
    (re-frame/dispatch [:wewe.events/set-position "" position])))

(def geo-options #js
    {:enableHighAccuracy true,
     :maximumAge         30000,
     :timeout            27000})

(defn fetch-position []
  (.watchPosition (.. js/navigator -geolocation) pos-success pos-error geo-options))

(re-frame.core/reg-fx
  :geolocation
  (fn-traced [_]
    (fetch-position)))



