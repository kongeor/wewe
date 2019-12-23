(ns wewe.handler
  (:require
    [compojure.route :as route]
    [compojure.core :refer [GET defroutes]]
    [compojure.route :refer [resources]]
    [ring.util.response :refer [resource-response response]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.logger :as logger]
    [shadow.http.push-state :as push-state]
    [wewe.db :as db]
    [wewe.weather :as weather]
    [ring.util.response :as response]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
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
    (json-response data)))

(defn weather-handler [request]
  ;; todo validate id
  (clojure.pprint/pprint request)
  (let [id (-> request :route-params :id str->int)
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
  (GET "/api/weather/:id" [] weather-handler)
  (route/not-found "404"))

(defn wrap-dir-index [handler]
  (fn [req]
    (handler
      (update
        req
        :uri
        #(if (= "/" %) "/index.html" %)))))

(defn wrap-exception [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (taoensso.timbre/fatal e)
           {:status 500
            :body "Oh no! :'("}))))

(def dev-handler
  (-> #'routes
    (wrap-defaults site-defaults)
    (wrap-reload push-state/handle)
    logger/wrap-with-logger
    wrap-exception))

(def handler
  (-> routes
    (wrap-defaults site-defaults)
    wrap-dir-index
    logger/wrap-with-logger
    wrap-exception))
