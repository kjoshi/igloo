(ns igloo.init
  (:require [igloo.db :as db]
            [igloo.features.common :as common]
            [re-frame.core :as rf]))

(defn add-local-items [db local-items]
  (cond-> db
    (seq local-items) (assoc :items local-items)))

(defn add-local-store-db [db local-store-db]
  (cond-> db
    (seq local-store-db) (merge local-store-db)))

(defn set-announcement-visibility [db]
  (cond-> db
    (< (:announcement-id-seen db) (:announcement-id db))
    (assoc :announcement-visible? true)))

(rf/reg-event-fx
 :app/initialize
 [(rf/inject-cofx :local-store-items)
  (rf/inject-cofx :local-store-db)
  common/check-spec-interceptor]
 (fn [{:keys [local-store-items local-store-db]} _]
   {:db (-> db/default-db
            (add-local-items local-store-items)
            (add-local-store-db local-store-db)
            (set-announcement-visibility))}))

