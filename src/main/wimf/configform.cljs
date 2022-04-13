(ns wimf.configform
  (:require [re-frame.core :as rf]
            [wimf.events :as events]
            [wimf.components :refer [close-form-button button]]
            [fork.re-frame :as fork]
            ["@headlessui/react" :refer (Dialog)]))

;; Helper functions
(defn next-id [sections]
  ((fnil inc 0) (:id (last sections))))

;;
;; Interceptors

(def form-interceptors
  [events/check-spec-interceptor
   events/db->local-store
   (rf/path :config-form)])

(def freezer-interceptors
  [events/check-spec-interceptor
   events/db->local-store
   (rf/path :freezer)])

;;
;; Events

(rf/reg-event-db
 :config-form/activate
 [form-interceptors]
 (fn [config _]
   (assoc config :active-mode :edit)))

(rf/reg-event-db
 :config-form/deactivate
 [form-interceptors]
 (fn [config _]
   (assoc config :active-mode :inactive)))

(defn form-vals->map [values]
  (reduce
   (fn [acc v]
     (assoc acc (:id v) v))
   {}
   values))

(rf/reg-event-fx
 :config-form/submit
 [freezer-interceptors]
 (fn [_ [_ {:keys [values]}]]
   (let [section-map (into (sorted-map) (form-vals->map (:freezer-sections values)))]
     {:db section-map
      :fx [[:dispatch [:config-form/deactivate]]]})))

;;
;; Subscriptions

(rf/reg-sub
 :config-form/active-mode
 (fn [db _]
   (-> db :config-form :active-mode)))

(rf/reg-sub
 :config-form/active?
 :<- [:config-form/active-mode]
 (fn [active-mode]
   (not= :inactive active-mode)))

;;
;; Components

(defn field-array-fn [{:keys [normalize-name]}
                      {:fieldarray/keys [fields name insert remove handle-change handle-blur]}]
  [:div
   [:div.divide-y.divide-slate-200
    (map-indexed
     (fn [idx field]
       ^{:key (str name idx)}
       [:div.grid.grid-cols-7.gap-x-1.gap-y-2.py-4
        [:label.block.text-gray-900.text-xl.font-medium.col-span-2.flex.items-center "Name"]
        [:input.form-input.col-span-5.flex.items-center {:name (normalize-name :name)
                                                         :type "text"
                                                         :value (get field :name)
                                                         :on-change #(handle-change % idx)
                                                         :on-blur #(handle-blur % idx)}]
        [:label.block.text-gray-900.text-xl.font-medium.col-span-2.flex.items-center "Colour"]
        [:div.col-span-2.flex.items-center
         [:input.form.input {:name (normalize-name :colour)
                             :type "color"
                             :value (get field :colour)
                             :on-change #(handle-change % idx)
                             :on-blur #(handle-blur % idx)}]]
        [:div.col-span-3.flex.items-center.justify-end
         [button {:type "button"
                  :on-click #(remove idx)
                  :width "w-32"
                  :text-size "text-sm"
                  :border-width "border-1"
                  :border-colour "border-gray-400"
                  :title "Remove section"}]]])
     fields)]
   [:div.flex.justify-end.items-center
    [button {:type "button"
             :on-click #(insert {:id (next-id fields) :name "New section" :colour "#ff0000"})
             :width "w-32"
             :text-size "text-sm"
             :border-width "border-1"
             :border-colour "border-gray-400"
             :title "Add section"}]]])

(defn fork-form []
  (let [freezer-sections (rf/subscribe [:freezer/sections])]
    (fn []
      [fork/form
       {:keywordize-keys true
        :form-id "config-form"
        :initial-values {:freezer-sections (vec (vals @freezer-sections))}
        :prevent-default? true
        :on-submit #(rf/dispatch [:config-form/submit %])}
       (fn [{:keys [form-id handle-submit] :as props}]
         [:form.grid.grid-cols-1.gap-6.grow-0.mt-1
          {:id form-id
           :on-submit handle-submit}
          [fork/field-array {:props props
                             :name :freezer-sections}
           field-array-fn]
          [button {:type "submit" :title "Save & Close"}]])])))

;;
;; Views

(defn config-form []
  (let [active? (rf/subscribe [:config-form/active?])
        show-locations? (rf/subscribe [:show-locations?])]
    (fn []
      [:> Dialog {:class "fixed inset-0 z-10 overflow-y-auto"
                  :open @active?
                  :onClose #(rf/dispatch [:config-form/deactivate])}
       [:div.min-h-screen.px-4.text-center
        [:> (.-Overlay Dialog) {:class "fixed inset-0 bg-black opacity-30"}]
        [:div.inline-block.w-full.max-w-md.p-6.my-8.overflow-hidden.text-left.align-middle.transition-all.transform.bg-white.shadow-xl.rounded-2xl
         [:> (.-Title Dialog) {:class "flex justify-between items-center"}
          [:div.text-2xl.font-bold.leading-6.text-gray-900 "Setup"]
          [close-form-button #(rf/dispatch [:config-form/deactivate])]]
         [:div.my-4.flex.items-center.text-xl
          [:label.pr-4 {:for "show-locations"} "Show freezer sections?"]
          [:input {:type "checkbox"
                   :id "show-locations"
                   :name "show-locations"
                   :on-change #(rf/dispatch [:toggle-show-locations])
                   :checked @show-locations?
                   :class ["w-6" "h-6"]}]]
         [:div.text-xl.text-gray-900.mt-2 "Setup freezer sections:"
          [fork-form]]]]])))

(comment

  (form-vals->map [{:id 1, :name "Top drawer", :colour "#048ba8"}
                   {:id 2, :name "Middle drawer", :colour "#ff715b"}
                   {:id 3, :name "Bottom drawer", :colour "#99c24d"}])

;
  )
