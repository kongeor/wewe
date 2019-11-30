(ns wewe.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
  ::position
  (fn [db _]
    (:position db)))

(re-frame/reg-sub
  ::weather
  (fn [db _]
    (:weather db)))

(re-frame/reg-sub
  ::cities
  (fn [db _]
    (:cities db)))

(re-frame/reg-sub
  ::selected-city
  (fn [db _]
    (:selected-city db)))
