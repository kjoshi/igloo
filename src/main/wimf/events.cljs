(ns wimf.events
  (:require [wimf.db :as db]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(defn check-and-throw [spec db]
  (when-not (s/valid? spec db)
    (throw (ex-info (str "spec check failed:" (s/explain-str spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :wimf.db/db)))

(def items->local-store (rf/after db/items->local-store))
(def db->local-store (rf/after db/db->local-store))
(def item-interceptors [check-spec-interceptor
                        db->local-store
                        (rf/path :items)
                        items->local-store])

#_(def freezer-interceptors [check-spec-interceptor
                           (rf/after db/db->local-store)
                           (rf/path :freezer)])

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
  (rf/inject-cofx :local-store-db)
  check-spec-interceptor]
 (fn [{:keys [local-store-items local-store-db]} _]
   {:db (cond-> db/default-db
          (seq local-store-items) (assoc :items local-store-items)
          (seq local-store-db) (merge local-store-db))}))

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

  (def local-store-items {})
  (def local-store-db {:sort-key :quantity})

  (cond-> db/default-db
    (seq local-store-items) (assoc :items local-store-items)
    (seq local-store-db) (merge local-store-db))

  (merge {} {:items ["a" "b" "c"]} {:sort-key :created :items [1 2 3]})

 ;; wimf-items
 ;; {1 {:id 1, :name "Tomato bolognese with mushrooms", :quantity 0, :created "2022-04-05"}, 2 {:id 2, :name "Daal with spinach", :quantity 3, :created "2022-04-02"}, 4 {:id 4, :name "Pea Soup", :quantity 3, :created "2022-04-10"}, 5 {:id 5, :name "Test", :quantity 2, :created "2022-04-10"}, 6 {:id 6, :name "test 4", :quantity 2, :created "2022-04-10"}, 7 {:id 7, :name "Chicken & sun dried tomato", :quantity 3, :created "2022-04-11"}, 8 {:id 8, :name "Testing", :quantity 3, :created "2022-04-11"}}

;
  )
