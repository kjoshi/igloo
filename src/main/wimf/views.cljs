(ns wimf.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [wimf.util :refer [date-string]]
            [wimf.components :as c]
            [clojure.string :as s]
            ["@headlessui/react" :refer (Dialog Menu)]))

(defn iinput [{:keys [value on-save]}]
  (let [val (r/atom (or value ""))
        save #(let [v (-> @val str s/trim)]
                (on-save v))]
    (fn [props]
      [:input (merge (dissoc props :value :on-save)
                     {:value @val
                      :on-blur save
                      :on-change #(reset! val (.. % -target -value))
                      :class "mt-2 block w-full rounded-md bg-gray-200 border-transparent focus:border-gray-500 focus:bg-white focus:ring-0"})])))

(defn input [{:keys [type attrs value on-save]}]
  (let [draft (r/atom nil)
        value (r/track #(or @draft @value ""))]
    (fn []
      [:input
       (merge attrs
              {:type type
               :on-focus #(reset! draft (or @value ""))
               :on-blur (fn []
                          (on-save (or @draft ""))
                          (reset! draft nil))
               :on-change #(reset! draft (.. % -target -value))
               :value @value
               :class "mt-2 block w-full rounded-md bg-gray-200 border-transparent focus:border-gray-500 focus:bg-white focus:ring-0"})])))

(defn text-input [{attrs :attrs
                   value :value
                   :keys [on-save]}]
  [input {:type :text
          :attrs attrs
          :value value
          :on-save on-save}])

(defn number-input [{attrs :attrs
                     value :value
                     :keys [on-save]}]
  [input {:type :number
          :attrs attrs
          :value value
          :on-save on-save}])

(defn date-input [props]
  [iinput (merge {:type :date} props)])

(defn svg [d-element props]
  (let [color "text-gray-900"]
    [:div (merge props {:class (str color " border border-2 rounded-xl border-current text-current p-2 font-medium hover:bg-gray-200")})
     [:svg {:xmlns "http://www.w3.org/2000/svg", :class "h-10 w-10", :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
      [:path {:stroke-linecap "round", :stroke-linejoin "round", :d d-element}]]]))

(defn svg-button
  ([d-element]
   (svg-button d-element {}))

  ([d-element props]
   (let [color (case (:color props)
                 :danger "text-red-500"
                 "text-gray-900")]
     [:button (merge props {:class (str color " border border-2 rounded-xl border-current text-current p-2 font-medium hover:bg-gray-200")})
      [:svg {:xmlns "http://www.w3.org/2000/svg", :class "h-10 w-10", :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
       [:path {:stroke-linecap "round", :stroke-linejoin "round", :d d-element}]]])))

(defn close-form-button [item-id]
  [svg-button "M6 18L18 6M6 6l12 12"
   {:on-click #(rf/dispatch [:form/deactivate item-id])}])

(defn item [{:keys [id name quantity created]}]
  (let [color-modifier (if (zero? quantity) "400" "900")]
    [:div {:class (str "border-gray-" color-modifier " grid grid-cols-5 gap-x-1 md:gap-x-6 border border-2 py-4 rounded-xl h-32 bg-white")}

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
      [svg-button "M18 12H6" {:on-click #(rf/dispatch [:item/decrement id])}]]]))

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
          [close-form-button (:id @fields)]]
         [c/fork-form]
         (when (:id @fields)
           [:button {:class "mt-4 text-white bg-red-500 border border-2 rounded-xl border-current py-2 text-xl font-medium w-full hover:bg-gray-200"
                     :on-click #(rf/dispatch [:item/delete (:id @fields)])}
            "Delete"])]]])))

(defn menu-item [{:keys [title on-click]}]
  [:> (.-Item Menu) {:as "div"}
   [:button {:class "text-lg text-gray-900 p-4"
             :on-click on-click}
    title]])

(defn header []
  [:div.w-full.flex.justify-between.items-center.py-4
   [:div {:class "text-2xl pr-8 font-bold"} "Freezer Tracker"]
   [:div.flex.justify-around.gap-x-8
    [:> Menu {:as "div" :class "relative inline-block"}
     [:> (.-Button Menu)
      [svg "M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4"]]
     [:> (.-Items Menu) {:class "absolute right-0 w-44 mt-2 origin-top-right bg-white divide-y divide-gray-100 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"}
      [menu-item {:title "Sort by name" :on-click #(rf/dispatch [:items/set-sort-key :name])}]
      [menu-item {:title "Sort by quantity" :on-click #(rf/dispatch [:items/set-sort-key :quantity])}]
      [menu-item {:title "Sort by date" :on-click #(rf/dispatch [:items/set-sort-key :created])}]]]

    [:div.grow-0
     [svg-button "M12 4v16m8-8H4" {:on-click #(rf/dispatch [:form/open-new])}]]]])

(defn main []
  (let [available-items (rf/subscribe [:items/sorted-list :available])
        empty-items (rf/subscribe [:items/sorted-list :empty])
        any-empty? (rf/subscribe [:items/any-empty?])]

    (fn []
      [:div.w-full.flex.justify-center.bg-slate-100.min-h-screen
       [:div {"className" "px-6 w-full max-w-xl"}
        [header]
        [:div
         [item-list available-items]
         (when @any-empty?
           [:<>
            [:hr]
            [item-list empty-items]])]]
       [item-form]])))

(comment

  (some? "")

  (dissoc {:a 1} nil)
  ;
  )









