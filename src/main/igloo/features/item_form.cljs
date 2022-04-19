(ns igloo.features.item-form
  (:require [re-frame.core :as rf]
            ["@headlessui/react" :refer (Dialog)]
            [fork.re-frame :as fork]
            [vlad.core :as vlad]
            [igloo.components :refer [button close-form-button]]
            [igloo.features.common :as common]
            [igloo.util :refer [current-date]]))

;;
;; Helpers
(def form-validation
  (vlad/join (vlad/attr [:name] (vlad/present))
             (vlad/attr [:quantity] (vlad/present))
             (vlad/attr [:created] (vlad/present))))

;;
;; Interceptors
(def form-interceptors [common/check-spec-interceptor
                        (rf/path :form)])

;;
;; Events
(rf/reg-event-db
 :form/set-fields
 [form-interceptors]
 (fn [form [_ fields]]
   (merge form fields)))

(rf/reg-event-db
 :form/clear-fields
 [form-interceptors]
 (fn [_ _]
   {:active-mode :inactive}))

(rf/reg-event-db
 :form/activate
 [form-interceptors]
 (fn [form [_ mode]]
   (assoc form :active-mode mode)))

(rf/reg-event-db
 :form/deactivate
 [form-interceptors]
 (fn [form _]
   (assoc form :active-mode :inactive)))

(rf/reg-event-fx
 :form/open-edit
 [common/check-spec-interceptor]
 (fn [_ [_ fields]]
   {:fx [[:dispatch [:form/set-fields fields]]
         [:dispatch [:form/activate :edit]]]}))

(rf/reg-event-fx
 :form/open-new
 [common/check-spec-interceptor]
 (fn [_ _]
   {:fx [[:dispatch [:form/clear-fields]]
         [:dispatch [:form/activate :new]]]}))

(rf/reg-event-fx
 :form/submit
 (fn [_ [_ {:keys [values]}]]
   (let [parsed-vals (cond-> values
                       (:quantity values) (update :quantity js/parseInt))]
     {:fx [[:dispatch [:items/add parsed-vals]]
           [:dispatch [:form/deactivate]]]})))

;;
;; Subscriptions
(rf/reg-sub
 :form/fields
 :-> :form)

(rf/reg-sub
 :form/active-mode
 (fn [db _]
   (-> db :form :active-mode)))

(rf/reg-sub
 :form/active?
 :<- [:form/active-mode]
 (fn [active-mode _]
   (not= :inactive active-mode)))

;;
;; Views
(defn item-form []
  (let [fields (rf/subscribe [:form/fields])
        css "mt-2 block w-full rounded-md bg-gray-200 border-transparent focus:border-gray-500 focus:bg-white focus:ring-0"]
    (fn []
      [fork/form
       {:path [:fork-form]
        :form-id "item-form"
        :prevent-default? true
        :clean-on-unmount? true
        :keywordize-keys true
        :initial-values {:id (:id @fields)
                         :name (:name @fields)
                         :quantity (str  (:quantity @fields))
                         :created (or (:created @fields) (current-date))}
        :validation #(vlad/field-errors form-validation %)
        :on-submit #(rf/dispatch [:form/submit %])}
       (fn [{:keys [form-id handle-submit values handle-change handle-blur errors touched]}]
         [:form {:class "grid grid-cols-1 gap-3 mt-8 grow-0"
                 :id form-id
                 :on-submit handle-submit}
          [:label.block
           [:div.text-gray-900.text-xl.font-medium "Name"]
           [:input {:type "text"
                    :class css
                    :name :name
                    :value (values :name)
                    :on-change handle-change
                    :on-blur handle-blur}]
           [:div.h-6.text-red-500
            (when (touched :name) (first (get errors (list :name))))]]
          [:label.block
           [:div.text-gray-900.text-xl.font-medium "How many"]
           [:input {:type "number"
                    :class css
                    :name :quantity
                    :value (values :quantity)
                    :on-change handle-change
                    :on-blur handle-blur}]
           [:div.h-6.text-red-500
            (when (touched :quantity) (first (get errors (list :quantity))))]]
          [:label.block
           [:div.text-gray-900.text-xl.font-medium "Cooked date"]
           [:input {:type "date"
                    :class css
                    :name :created
                    :value (values :created)
                    :on-change handle-change
                    :on-blur handle-blur}]
           [:div.h-6.text-gray-400
            (when (touched :created) [:span.text-red-500 (first (get errors (list :created)))])]]

          [button {:type "submit" :title "Save"}]])])))

(defn item-dialog []
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
         [item-form]
         (when (:id @fields)
           [:div.mt-2
            [button {:title "Delete"
                     :type "button"
                     :bg-colour "bg-red-500"
                     :text-colour "text-white"
                     :hover-colour "hover:bg-red-700"
                     :on-click #(rf/dispatch [:item/delete (:id @fields)])}]])]]])))
