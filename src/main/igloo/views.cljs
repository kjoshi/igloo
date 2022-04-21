(ns igloo.views
  (:require [re-frame.core :as rf]
            [igloo.components :refer [icon-button]]
            [igloo.features.configuration :refer [config-form]]
            [igloo.features.item :refer [item]]
            [igloo.features.item-form :refer [item-dialog]]
            [igloo.features.announcement :refer [announcement-dialog]]
            ["@headlessui/react" :refer (Menu)]))

(defn svg [d-element props]
  (let [color "text-gray-900"]
    [:div (merge props {:class (str color " border border-2 rounded-xl border-current text-current p-2 font-medium hover:bg-gray-200")})
     [:svg {:xmlns "http://www.w3.org/2000/svg", :class "h-8 w-8", :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
      [:path {:stroke-linecap "round", :stroke-linejoin "round", :d d-element}]]]))

(defn open-new-button []
  [:div.text-gray-900
   [icon-button  "M12 4v16m8-8H4"
    {:on-click #(rf/dispatch [:form/open-new])}]])

(defn settings-button []
  [:div.grow-0
   [:button {:class "text-gray-900 border border-2 rounded-xl border-current text-current p-2 font-medium hover:bg-gray-200 grow-0"
             :on-click #(rf/dispatch [:config-form/activate])}
    [:svg {:xmlns "http://www.w3.org/2000/svg", :class "h-8 w-8", :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
     [:path {:stroke-linecap "round", :stroke-linejoin "round", :d "M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"}]
     [:path {:stroke-linecap "round", :stroke-linejoin "round", :d "M15 12a3 3 0 11-6 0 3 3 0 016 0z"}]]]])

(defn item-list [items]
  [:div.w-full.space-y-5.py-4
   (for [i @items]
     ^{:key (:id i)}
     [item i])])

(defn menu-item [{:keys [title on-click]}]
  [:> (.-Item Menu) {:as "div"}
   [:button {:class "text-lg text-gray-900 p-4"
             :on-click on-click}
    title]])

(defn header []
  [:div.w-full.flex.justify-between.items-center.py-4
   [:div {:class "text-2xl pr-8 font-bold"} "Igloo"]
   [:div.flex.justify-around.gap-x-4
    [settings-button]
    [:> Menu {:as "div" :class "relative inline-block"}
     [:> (.-Button Menu)
      [svg "M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4"]]
     [:> (.-Items Menu) {:class "absolute right-0 w-44 mt-2 origin-top-right bg-white divide-y divide-gray-100 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"}
      [menu-item {:title "Sort by name" :on-click #(rf/dispatch [:config/set-sort-key :name])}]
      [menu-item {:title "Sort by quantity" :on-click #(rf/dispatch [:config/set-sort-key :quantity])}]
      [menu-item {:title "Sort by date" :on-click #(rf/dispatch [:config/set-sort-key :created])}]]]
    [:div.grow-0
     [open-new-button]]]])

(defn main []
  (let [available-items (rf/subscribe [:items/sorted-list :available])
        empty-items (rf/subscribe [:items/sorted-list :empty])
        any-empty? (rf/subscribe [:items/any-empty?])]

    (fn []
      [:div.w-full.flex.justify-center.bg-slate-100.min-h-screen
       [:div {"className" "px-6 w-full max-w-xl"}
        [header]
        [item-list available-items]
        (when @any-empty?
          [:<>
           [:hr]
           [item-list empty-items]])]
       [item-dialog]
       [announcement-dialog]
       [config-form]])))
