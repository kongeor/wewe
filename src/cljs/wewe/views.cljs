(ns wewe.views
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
    [wewe.subs :as subs]
    [wewe.util :as util]
    ))

;var d = new Date();
;
;// convert to msec
;// add local time zone offset
;// get UTC time in msec
;var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
;
;// create new Date object for different city
;// using supplied offset
;var nd = new Date(utc + (3600000*offset));

#_(defn get-timezone-time [millis-time country-code]
  (let [tz-name (-> (. cttz getCountry country-code) (js->clj :keywordize-keys true) :timezones first)
        tz (. cttz getTimezone tz-name)
        country-tz-offset (-> (js->clj tz :keywordize-keys true) :utcOffset)
        d (js/Date. (* millis-time 1000))
        time (.getTime d)
        tz-offset (.getTimezoneOffset d)
        utc (+ time (* 60000 tz-offset))
        tz-date (js/Date. (+ utc (* 3600000 (/ country-tz-offset 60))))]
    [d (js/Date utc)]
    #_(js/Date. millis-time )
    ))

(defn fmt-digit [d]
  (if (< d 10)
    (str "0" d)
    d))

;; TODO tz?
(defn fmt-date [millis-time]
  (let [d (js/Date. (* millis-time 1000))]
    (str (.getHours d) ":" (fmt-digit (.getMinutes d)))))

#_ (println (get-timezone-time 1576820868 "GR"))
#_(println (fmt-date 1576820868 ))

;; home

(defn search-component []
  (let [search (reagent/atom "")]
    (fn []
      (let [cities @(re-frame/subscribe [::subs/cities])
            selected-city @(re-frame/subscribe [::subs/selected-city])
            clazz (if (empty? cities) "field" "field is-active")]
        [:div.field
         [:div.dropdown {:class clazz}
          [:div.dropdown-trigger
           [:input.input {:type "text"
                          :placeholder "Thessaloniki"
                          :aria-controls "dropdown-menu"
                          :on-change #(let [val (-> % .-target .-value)]
                                        (reset! search val)
                                        (if (clojure.string/blank? val)
                                          (re-frame/dispatch
                                            [:wewe.events/reset-cities])
                                          (re-frame/dispatch
                                            [:wewe.events/set-position val {}])))
                          :value @search}]]
          [:div.dropdown-menu {:role "menu"
                               :id "dropdown-menu"}
           [:div.dropdown-content
            (for [city cities]
              ^{:key city} [:a.dropdown-item
                            {:on-click #(do
                                          (reset! search (:name city))
                                          (re-frame/dispatch [:wewe.events/select-city city]))}
                            (:name city)])]]]
         #_[:div.field
          [:label.checkbox
           [:input {:type "checkbox"
                    :on-click #(util/fetch-position)}]
           " Use browser location"]]]))))

(defn icon-link [icon]
  (str "http://openweathermap.org/img/wn/" icon "@2x.png"))

(defn home-panel []
  [:div
   [search-component]
   (if-let [weather @(re-frame/subscribe [::subs/weather])]
     [:div.card
      [:header.card-header
       [:p.card-header-title (:name weather)]
       [:a.card-header-icon {:href "#" :aria-label "favorite"}
        [:span.icon
         [:i.far.fa-star {:aria-hidden true}]]]]
      [:div.card-content
       [:div.media
        [:div.media-left
         [:figure.image.is-48x48
          [:img {:src (icon-link (-> weather :weather first :icon))
                 :alt (-> weather :weather first :description)}]]]
        [:div.media-content
         [:p.title.is-4
          (str (-> weather :weather first :main)
            " " (-> weather :main :temp Math/round) "°C")]
         #_[:p (str (:main weather))]
         [:p "Min: " (-> weather :main :temp_min Math/round)]
         [:p "Max: " (-> weather :main :temp_max Math/round)]
         [:p "Humidity: " (-> weather :main :humidity)]
         [:p "Pressure: " (-> weather :main :pressure)]
         #_[:p (str (:sys weather))]
         [:p "Sunrise: " (fmt-date (-> weather :sys :sunrise))]
         [:p "Sunset: " (fmt-date (-> weather :sys :sunset))]
         ]]]])])


;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:section.section
     [:div.container
      [:div.columns.is-centered
       [:div.column.is-half
        [:h1.title "wewe!"]
        [show-panel @active-panel]
        #_[:footer.footer
         [:div.content.has-text-centered
          [:p "ugly long footer"]]]]]]]))
