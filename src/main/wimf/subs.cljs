(ns wimf.subs
  (:require [re-frame.core :as rf]
            [wimf.db :as db]))

(rf/reg-sub
 :sort-key
 (fn [db _]
   (:sort-key db)))

(rf/reg-sub
 :reverse-sort?
 (fn [db _]
   (:reverse-sort? db)))

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
    (rf/subscribe [:sort-key])
    (rf/subscribe [:reverse-sort?])])
 (fn [[items sort-key reverse?] _]
   (cond->> (sort-by sort-key (vals items))
     reverse? reverse)))

(rf/reg-sub
 :form/fields
 :-> :form)

(rf/reg-sub
 :form/field
 :<- [:form/fields]
 (fn [fields [_ key]]
   (key fields)))

(rf/reg-sub
 :form/active-mode
 (fn [db _]
   (-> db :form :active-mode)))

(rf/reg-sub
 :form/active-new?
 :<- [:form/active-mode]
 (fn [active-mode _]
   (= :new active-mode)))

(rf/reg-sub
 :form/active-edit?
 :<- [:form/active-mode]
 (fn [active-mode _]
   (= :edit active-mode)))

(rf/reg-sub
 :form/active?
 :<- [:form/active-mode]
 (fn [active-mode _]
   (not= :inactive active-mode)))

(comment

  (into (sorted-map) (filter (fn [[_ {quantity :quantity}]] (pos? quantity)) (:items db/default-db)))

  (boolean nil)

  (boolean :new)

  ;
  )
