(ns wewe.views
  (:require
    [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [wewe.subs :as subs]
   [wewe.util :as util]
   ))


;; home

(defn search-component []
  (let [search (reagent/atom "")]
    (fn []
      (let [cities @(re-frame/subscribe [::subs/cities])
            selected-city @(re-frame/subscribe [::subs/selected-city])
            clazz (if (empty? cities) "" "is-active")]
        [:div.dropdown {:class clazz}
         [:div.dropdown-trigger
          [:input.input {:type          "text"
                         :placeholder   "Thessaloniki"
                         :aria-controls "dropdown-menu"
                         :on-change     #(let [val (-> % .-target .-value)]
                                           (reset! search val)
                                           (re-frame/dispatch
                                             [:wewe.events/set-position val {}]))
                         :value         @search}]]
         [:div.dropdown-menu {:role "menu"
                              :id   "dropdown-menu"}
          [:div.dropdown-content
           (for [city cities]
             ^{:key city} [:a.dropdown-item
                           {:on-click #(do
                                         (reset! search (:name city))
                                         (re-frame/dispatch [:wewe.events/select-city city]))}
                           (:name city)])]]]))))

(defn home-panel []
  (let [weather @(re-frame/subscribe [::subs/weather])]
    [:div
     [search-component]
     [:div.card
      [:div.card-content
       [:div.media
        [:div.media-left
         [:i.fa.fa-bolt]
         #_[:figure.image.is-48x48
          [:img {:src "https://bulma.io/images/placeholders/96x96.png"
                 :alt "placeholder"}]]]
        [:div.media-content
         [:p.title.is-4 (:name weather)]
         [:p (str (:weather weather))]
         [:p (str (:sys weather))]
         [:p (str (:main weather))]]]]]]))


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
      [:h1.title "wewe!"]
      [show-panel @active-panel]]]))
