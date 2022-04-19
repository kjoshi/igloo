(ns wimf.common
  (:require [wimf.db :as db]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(defn check-and-throw [spec db]
  (when-not (s/valid? spec db)
    (throw (ex-info (str "spec check failed:" (s/explain-str spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :wimf.db/db)))

(def db->local-store (rf/after db/db->local-store))


(rf/reg-event-fx
 :app/initialize
 [(rf/inject-cofx :local-store-items)
  (rf/inject-cofx :local-store-db)
  check-spec-interceptor]
 (fn [{:keys [local-store-items local-store-db]} _]
   {:db (cond-> db/default-db
          (seq local-store-items) (assoc :items local-store-items)
          (seq local-store-db) (merge local-store-db))}))


