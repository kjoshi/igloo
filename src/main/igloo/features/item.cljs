(ns igloo.features.item
  (:require
   [re-frame.core :as rf]
   [igloo.db :as db]
   [igloo.components :refer [icon-button]]
   [igloo.features.common :as common]
   [igloo.util :refer [date-string]]))

;;
;; Helper functions 
(defn allocate-next-id [items]
  ((fnil inc 0) (last (keys items))))

;;
;; Interceptors 
(def items->local-store (rf/after db/items->local-store))
(def item-interceptors [common/check-spec-interceptor
                        common/db->local-store
                        (rf/path :items)
                        items->local-store])

;;
;; Events
(defn toggle-id [id current-ids]
  (let [id-set (hash-set id)]
    (if (some id-set current-ids)
      (vec (remove id-set current-ids))
      (conj current-ids id))))

(rf/reg-event-db
 :item/toggle-section
 [item-interceptors]
 (fn [items [_ item-id section-id]]
   (let [current-ids (get-in items [item-id :section-ids] [])]
     (assoc-in items [item-id :section-ids] (toggle-id section-id current-ids)))))

(rf/reg-event-db
 :item/decrement
 [item-interceptors]
 (fn [items [_ id]]
   (let [quantity (get-in items [id :quantity])
         new-quantity (max 0 (dec quantity))]
     (assoc-in items [id :quantity] new-quantity))))

(rf/reg-event-db
 :items/add
 [item-interceptors]
 (fn [items [_ item]]
   (let [item-id (or (:id item) (allocate-next-id items))
         full-item (cond-> item
                     (not (:id item))      (assoc :id item-id)
                     (not (:created item)) (assoc :created (js/Date.)))]
     (assoc items item-id full-item))))

(rf/reg-event-fx
 :item/delete
 [item-interceptors]
 (fn [{:keys [db]} [_ id]]
   {:db (dissoc db id)
    :fx [[:dispatch [:form/clear-fields]]]}))

(rf/reg-event-db
 :config/set-sort-key
 [common/check-spec-interceptor]
 (fn [db [_ key]]
   (assoc db :sort-key key)))

;;
;; Subscriptions
(rf/reg-sub
 :items/list
 (fn [db _]
   (:items db)))

(defn quantity-filter
  [[_ {quantity :quantity}]]
  (pos? quantity))

(rf/reg-sub
 :items/available-list
 :<- [:items/list]
 (fn [items _]
   (filter quantity-filter items)))

(rf/reg-sub
 :items/empty-list
 :<- [:items/list]
 (fn [items _]
   (filter (complement quantity-filter) items)))

(rf/reg-sub
 :items/any-empty?
 :<- [:items/empty-list]
 (fn [items _]
   (pos? (count items))))

(rf/reg-sub
 :items/sorted-list
 (fn [[_ type] _]
   [(if (= :empty type)
      (rf/subscribe [:items/empty-list])
      (rf/subscribe [:items/available-list]))
    (rf/subscribe [:config/sort-key])
    (rf/subscribe [:config/reverse-sort?])])
 (fn [[items sort-key reverse?] _]
   (cond->> (sort-by sort-key (vals items))
     reverse? reverse)))

;;
;; Components

(defn section-indicator [item-id section-id present? colour]
  [:div {:class ["border" "border-2" "rounded-r-sm" "h-4" "border-gray-900" "cursor-pointer"]
         :style (if present? {:background-color colour} {})
         :on-click #(rf/dispatch [:item/toggle-section item-id section-id])}])

(defn decrement-button [item-id]
  [:div.text-gray-900
   [icon-button  "M18 12H6"
    {:on-click #(rf/dispatch [:item/decrement item-id])}]])

;;
;; Views
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
  (let [show-locations? (rf/subscribe [:config/show-locations?])]
    (fn [{:keys [id name quantity created section-ids]}]
      [:div {:class ["flex" "justify-start" "border" "border-2" "py-4" "rounded-xl" "min-h-[7rem]" "bg-white" (if (zero? quantity) "border-gray-400" "border-gray-900")]}
       (when (and @show-locations? (pos? quantity))
         [item-sections id section-ids])
       [:div {:class "grid grid-cols-5 gap-x-1 mg:gap-x-6 flex-auto"}
        [:div.col-span-3.cursor-pointer.flex.flex-col.justify-center.items-center.text-center.gap-1.px-2
         {:on-click #(rf/dispatch [:form/open-edit {:id id
                                                    :name name
                                                    :quantity quantity
                                                    :created created}])}
         [:div {:class ["select-none" "text-lg" "font-bold" (if (zero? quantity) "text-gray-400" "text-gray-900")]} name]
         [:div {:class ["select-none" "text-lg" "font-medium" (if (zero? quantity) "text-gray-400" "text-gray-900")]} (date-string created)]]

        [:div {:class ["col-span-2" "flex" "justify-between" "items-center" "pr-4" (if (zero? quantity) "text-gray-400 border-gray-400" "text-gray-900 border-gray-900")]}
         [:div.text-5xl.font-medium
          quantity]
         (when (pos? quantity) [decrement-button id])]]])))
