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
  (when s
    (Double. s)))

(defn str->int [s]
  (when s
    (Integer. s)))

(defn json-response [data]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data true)})

(defn cities-handler [request]
  (let [lat (-> request :params :lat str->double)
        lon (-> request :params :lon str->double)
        radius (or (-> request :params :radius str->int) 10)
        data (vec (db/cities-in-radius lat lon radius))]
    (json-response data)))

(defn weather-handler [request]
  ;; todo validate id
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

(def dev-handler
  (-> #'routes
    (wrap-defaults api-defaults)
    (wrap-reload push-state/handle)))

(def handler
  (-> routes
    (wrap-defaults api-defaults)))
