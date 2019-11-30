(ns wewe.handler
  (:require
    [compojure.core :refer [GET defroutes]]
    [compojure.route :refer [resources]]
    [ring.util.response :refer [resource-response response]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.reload :refer [wrap-reload]]
    [shadow.http.push-state :as push-state]
    [wewe.db :as db]
    [wewe.weather :as weather]
    [ring.util.response :as response]
    [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
    [cheshire.core :as json]))

;; utils

(defn str->double [s]
  (when-not (clojure.string/blank? s)
    (Double. s)))

(defn str->int [s]
  (when-not (clojure.string/blank? s)
    (Integer. s)))

(defn json-response [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data true)})

(defn cities-handler [request]
  (let [lat (-> request :params :lat str->double)
        lon (-> request :params :lon str->double)
        name (-> request :params :name)
        name (str name "*")
        radius (or (-> request :params :radius str->int) 10)
        data (vec (db/search-city-data name :lat lat :lon lon :radius radius))]
    (println "searching cities" (str name "*") lat lon radius)
    (json-response data)))

(defn weather-handler [request]
  ;; todo validate id
  (println "(" (-> request :params :id str->int))
  (let [id (-> request :params :id str->int)
        cached (db/get-weather id)]
    (if cached
      (json-response cached)
      (if-let [weather (weather/get-weather id)]
        (do
          (db/set-weather id weather)
          (json-response weather))))))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/api/cities" [] cities-handler)
  (GET "/api/weather" [] weather-handler)
  (resources "/"))

(defn wrap-exception [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (taoensso.timbre/fatal e)
           {:status 500
            :body "Oh no! :'("}))))

(def dev-handler
  (-> #'routes
    (wrap-defaults api-defaults)
    (wrap-reload push-state/handle)
    wrap-exception))

(def handler
  (-> routes
    (wrap-defaults api-defaults)
    wrap-exception))
