(ns wewe.events
  (:require
   [re-frame.core :as re-frame]
   [wewe.db :as db]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   ))

(re-frame/reg-event-fx
 ::initialize-db
 (fn-traced [_ _]
   {:db db/default-db
    ; :geolocation nil ; TODO check
    }))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
  ::reset-cities
  (fn-traced [db [_ _]]
    (assoc db :cities [])))

(re-frame/reg-event-fx
  ::set-position
  (fn-traced [{:keys [db]} [_ position]]
    {:db (assoc db :position position)
     :dispatch [::fetch-cities position]}))

(re-frame/reg-event-fx
  ::fetch-cities
  (fn-traced [{:keys [db]} [_ name pos]]
    {:db   (assoc db :fetching-cities true)
     :http-xhrio {:method          :get
                  :uri             (str "/api/cities?name=" name "&lat=" (-> pos :lat) "&lon=" (-> pos :lon))
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::success-fetching-cities]
                  :on-failure      [::failed-fetching-cities]}}))

(re-frame/reg-event-fx
  ::success-fetching-cities
  (fn-traced [{:keys [db]} [_ result]]
    {:db (assoc db :cities result :fetching-cities false)
     ; :dispatch [::fetch-weather (-> result first :id)]
     }
    ))

(re-frame/reg-event-db
  ::failed-fetching-cities
  (fn-traced [db [_ _]]
    (assoc db :fetching-cities false)))

(re-frame/reg-event-fx
  ::fetch-weather
  (fn-traced [{:keys [db]} [_ id]]
    {:db   (assoc db :fetching-weather true)
     :http-xhrio {:method          :get
                  :uri             (str "/api/weather/" id)
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::success-fetching-weather]
                  :on-failure      [::failed-fetching-weather]}}))

(re-frame/reg-event-db
  ::success-fetching-weather
  (fn-traced [db [_ result]]
    (assoc db :weather result :fetching-weather false)))

(re-frame/reg-event-db
  ::failed-fetching-weather
  (fn-traced [db [_ _]]
    (assoc db :fetching-weather false)))

(re-frame/reg-event-fx
  ::select-city
  (fn-traced [{:keys [db]} [_ city]]
    {:db (assoc db :selected-city city :cities [])
     :dispatch [::fetch-weather (-> city :id)]
     }
    ))
