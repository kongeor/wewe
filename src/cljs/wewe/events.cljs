(ns wewe.events
  (:require
   [re-frame.core :as re-frame]
   [wewe.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   ))

(re-frame/reg-event-fx
 ::initialize-db
 (fn-traced [_ _]
   {:db db/default-db
    :geolocation nil}))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
  ::set-position
  (fn-traced [db [_ position]]
    (assoc db :position position)))