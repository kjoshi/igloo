(ns wimf.views
  (:require [re-frame.core :as rf]
            [wimf.util :refer [date-string]]
            [wimf.components :refer [fork-form button icon-button close-form-button]]
            [wimf.configform :refer [config-form]]
            ["@headlessui/react" :refer (Dialog Menu)]))

(defn svg [d-element props]
  (let [color "text-gray-900"]
    [:div (merge props {:class (str color " border border-2 rounded-xl border-current text-current p-2 font-medium hover:bg-gray-200")})
     [:svg {:xmlns "http://www.w3.org/2000/svg", :class "h-8 w-8", :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
      [:path {:stroke-linecap "round", :stroke-linejoin "round", :d d-element}]]]))

(defn open-new-button []
  [:div.text-gray-900
   [icon-button  "M12 4v16m8-8H4"
    {:on-click #(rf/dispatch [:form/open-new])}]])

(defn decrement-button [item-id]
  [:div.text-gray-900
   [icon-button  "M18 12H6"
    {:on-click #(rf/dispatch [:item/decrement item-id])}]])

(defn settings-button []
  [:div.grow-0
   [:button {:class "text-gray-900 border border-2 rounded-xl border-current text-current p-2 font-medium hover:bg-gray-200 grow-0"
             :on-click #(rf/dispatch [:config-form/activate])}
    [:svg {:xmlns "http://www.w3.org/2000/svg", :class "h-8 w-8", :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
     [:path {:stroke-linecap "round", :stroke-linejoin "round", :d "M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"}]
     [:path {:stroke-linecap "round", :stroke-linejoin "round", :d "M15 12a3 3 0 11-6 0 3 3 0 016 0z"}]]]])

(defn section-indicator [item-id section-id present? colour]
  [:div {:class ["border" "border-2" "rounded-r-sm" "h-4" "border-gray-900" "cursor-pointer"]
         :style (if present? {:background-color colour} {})
         :on-click #(rf/dispatch [:item/toggle-section item-id section-id])}])

(defn item-sections []
  (let [freezer-sections (rf/subscribe [:freezer/sections])]
    (fn [item-id active-section-ids]
      [:div {:class "w-4 flex flex-col flex-none justify-between -ml-0.5"}
       (doall (for [section-id (keys @freezer-sections)]
                ^{:key (str "item-" item-id "-section-" section-id)}
                [section-indicator
                 item-id
                 section-id
                 (some (hash-set section-id) active-section-ids)
                 (get-in @freezer-sections [section-id :colour])]))])))

(defn item []
  (let [show-locations? (rf/subscribe [:show-locations?])]
    (fn [{:keys [id name quantity created section-ids]}]
      (let [color-modifier (if (zero? quantity) "400" "900")]
        [:div {:class (str "flex justify-start border border-2 py-4 rounded-xl min-h-[7rem] bg-white " "border-gray-" color-modifier)}
         (when (and @show-locations? (pos? quantity))
           [item-sections id section-ids])
         [:div {:class "grid grid-cols-5 gap-x-1 mg:gap-x-6 flex-auto"}
          [:div.col-span-3.cursor-pointer.flex.flex-col.justify-center.items-center.text-center.gap-1.px-2
           {:on-click #(rf/dispatch [:form/open-edit {:id id
                                                      :name name
                                                      :quantity quantity
                                                      :created created}])}
           [:div {:class (str "text-gray-" color-modifier " text-lg font-bold")} name]
           [:div {:class (str "text-gray-" color-modifier " text-lg font-medium")} (date-string created)]]

          [:div {:class (str "text-gray-" color-modifier " border-gray-" color-modifier " col-span-2 flex justify-between items-center pr-4")}
           [:div.text-5xl.font-medium
            quantity]
           [decrement-button id]]]]))))

(defn item-list [items]
  [:div.w-full.space-y-5.py-4
   (for [i @items]
     ^{:key (:id i)}
     [item i])])

(defn item-form []
  (let [active? (rf/subscribe [:form/active?])
        fields (rf/subscribe [:form/fields])]
    (fn []
      [:> Dialog {:class "fixed inset-0 z-10 overflow-y-auto"
                  :open @active?
                  :onClose #(rf/dispatch [:form/deactivate])}
       [:div.min-h-screen.px-4.text-center
        [:> (.-Overlay Dialog) {:class "fixed inset-0 bg-black opacity-30"}]
        [:div.inline-block.w-full.max-w-md.p-6.my-8.overflow-hidden.text-left.align-middle.transition-all.transform.bg-white.shadow-xl.rounded-2xl
         [:> (.-Title Dialog) {:class "flex justify-between items-center"}
          [:div.text-2xl.font-bold.leading-6.text-gray-900 (if (:id @fields) "Edit a meal" "Add a meal")]
          [close-form-button #(rf/dispatch [:form/deactivate])]]
         [fork-form]
         (when (:id @fields)
           [:div.mt-2
            [button {:title "Delete"
                     :type "button"
                     :bg-colour "bg-red-500"
                     :text-colour "text-white"
                     :hover-colour "hover:bg-red-700"
                     :on-click #(rf/dispatch [:item/delete (:id @fields)])}]])]]])))

(defn menu-item [{:keys [title on-click]}]
  [:> (.-Item Menu) {:as "div"}
   [:button {:class "text-lg text-gray-900 p-4"
             :on-click on-click}
    title]])

(defn header []
  [:div.w-full.flex.justify-between.items-center.py-4
   [:div {:class "text-2xl pr-8 font-bold"} "Freezer Tracker"]
   [:div.flex.justify-around.gap-x-4
    [settings-button]
    [:> Menu {:as "div" :class "relative inline-block"}
     [:> (.-Button Menu)
      [svg "M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4"]]
     [:> (.-Items Menu) {:class "absolute right-0 w-44 mt-2 origin-top-right bg-white divide-y divide-gray-100 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"}
      [menu-item {:title "Sort by name" :on-click #(rf/dispatch [:items/set-sort-key :name])}]
      [menu-item {:title "Sort by quantity" :on-click #(rf/dispatch [:items/set-sort-key :quantity])}]
      [menu-item {:title "Sort by date" :on-click #(rf/dispatch [:items/set-sort-key :created])}]]]

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
       [item-form]
       [config-form]])))

(comment

  (some? "")

  (dissoc {:a 1} nil)

  ;
  )










