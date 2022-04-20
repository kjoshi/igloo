(ns igloo.features.announcement
  (:require [re-frame.core :as rf]
            [igloo.features.common :as common]
            [igloo.components :refer [close-form-button]]
            ["@headlessui/react" :refer (Dialog)]))

;;
;; Subscriptions
(rf/reg-sub
 :announcement/visible?
 :-> :announcement-visible?)

;;
;; Events
(rf/reg-event-fx
 :announcement/open
 [common/check-spec-interceptor
  common/db->local-store]
 (fn [{:keys [db]} _]
   {:db (assoc db :announcement-visible? true)
    :fx [[:dispatch [:config-form/deactivate]]]}))

(rf/reg-event-fx
 :announcement/close
 [common/check-spec-interceptor
  common/db->local-store]
 (fn [{:keys [db]} _]
   {:db (assoc db :announcement-visible? false)
    :fx [[:dispatch [:announcement/set-seen-latest]]]}))

(rf/reg-event-db
 :announcement/set-seen-latest
 [common/check-spec-interceptor
  common/db->local-store]
 (fn [db _]
   (assoc db :announcement-id-seen (:announcement-id db))))

;;
;; Views

(defn announcement-dialog []
  (let [visible? (rf/subscribe [:announcement/visible?])]
    (fn []
      [:> Dialog {:class "fixed inset-0 z-20 overflow-y-auto"
                  :open @visible?
                  :onClose #(rf/dispatch [:announcement/close])}
       [:div.min-h-screen.px-4.text-center
        [:> (.-Overlay Dialog) {:class "fixed inset-0 bg-black opacity-30"}]
        [:div.inline-block.w-full.max-w-md.p-6.my-8.overflow-hidden.text-left.align-middle.transition-all.transform.bg-white.shadow-xl.rounded-2xl
         [:> (.-Title Dialog) {:class "flex justify-between items-center"}
          [:div.text-2xl.font-bold.leading-6.text-gray-900 "Welcome to Igloo"]
          [close-form-button #(rf/dispatch [:announcement/close])]]
         [:div.my-4.text-xl
          [:div.text-gray-900 "Igloo is an app for helping you keep track of what's in your freezer and how long it's been there for."]
          [:div.mt-1 "We use it to keep track of our batch-cooked meals."]
          [:div.mt-2.font-bold "Instructions:"]
          [:div.mt-1 "Add a new meal using the \"+\" button."]
          [:div.mt-1 "Once something's been taken out of the freezer, reduce the amount with the \"-\" button."]
          [:div.mt-1 "Edit or delete a meal by tapping on its name."]
          [:div.mt-1 "The small boxes to the left indicate where in the freezer the meal is (e.g. top drawer, middle drawer, bottom draw)."]
          [:div "The number of boxes and their colours can be changed in the setup screen (gear icon)."]]]]])))

