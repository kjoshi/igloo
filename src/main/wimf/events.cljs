(ns wimf.events
  (:require [wimf.db :as db]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(defn check-and-throw [spec db]
  (when-not (s/valid? spec db)
    (throw (ex-info (str "spec check failed:" (s/explain-str spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :wimf.db/db)))

(def items->local-store (rf/after db/items->local-store))
(def item-interceptors [check-spec-interceptor
                        (rf/path :items)
                        items->local-store])

(def freezer->local-store (rf/after db/freezer->local-store))
(def freezer-interceptors [check-spec-interceptor
                           (rf/path :freezer)
                           freezer->local-store])

(def form-interceptors [check-spec-interceptor
                        (rf/path :form)])

(defn allocate-next-id [items]
  ((fnil inc 0) (last (keys items))))

;; Form events

(rf/reg-event-db
 :form/set-field
 [form-interceptors]
 (fn [form [_ key value]]
   (assoc form key value)))

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
 [check-spec-interceptor]
 (fn [_ [_ fields]]
   {:fx [[:dispatch [:form/set-fields fields]]
         [:dispatch [:form/activate :edit]]]}))

(rf/reg-event-fx
 :form/open-new
 [check-spec-interceptor]
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

;; Item events

(rf/reg-event-db
 :items/add
 [item-interceptors]
 (fn [items [_ item]]
   (let [item-id (or (:id item) (allocate-next-id items))
         full-item (cond-> item
                     (not (:id item))      (assoc :id item-id)
                     (not (:created item)) (assoc :created (js/Date.)))]
     (println full-item)
     (assoc items item-id full-item))))

(rf/reg-event-db
 :item/decrement
 [item-interceptors]
 (fn [items [_ id]]
   (let [quantity (get-in items [id :quantity])
         new-quantity (max 0 (dec quantity))]
     (assoc-in items [id :quantity] new-quantity))))

(rf/reg-event-fx
 :item/delete
 [item-interceptors]
 (fn [{:keys [db]} [_ id]]
   {:db (dissoc db id)
    :fx [[:dispatch [:form/clear-fields]]]}))

(rf/reg-event-db
 :items/set-sort-key
 [check-spec-interceptor]
 (fn [db [_ key]]
   (assoc db :sort-key key)))

(rf/reg-event-db
 :items/toggle-reverse-sort
 [check-spec-interceptor]
 (fn [db [_ _]]
   (update db :reverse-sort? not)))

;; App events

(rf/reg-event-fx
 :app/initialize
 [(rf/inject-cofx :local-store-items)
  (rf/inject-cofx :local-store-freezer)
  check-spec-interceptor]
 (fn [{:keys [local-store-items local-store-freezer]} _]
   {:db (assoc db/default-db :items (merge (:items db/default-db) local-store-items)
               :freezer (merge (:freezer db/default-db) local-store-freezer))}))

(comment
  db/default-db

  (.now js/Date)
  (js/Date.)

  (update-in db/default-db [:items] dissoc 1)

  (update {:name "t" :quantityz "4"} :quantity js/parseInt)

  (def m {:name "t" :quantityz "3"})
  m

  (cond-> m
    (:quantity m) (update :quantity js/parseInt))

;
  )
