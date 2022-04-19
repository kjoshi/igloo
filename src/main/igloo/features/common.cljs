(ns igloo.features.common
  (:require [clojure.spec.alpha :as s]
            [igloo.db :as db]
            [re-frame.core :as rf]))

;;
;; Helpers

(defn check-and-throw [spec db]
  (when-not (s/valid? spec db)
    (throw (ex-info (str "spec check failed:" (s/explain-str spec db)) {}))))

;;
;; Interceptors

(def check-spec-interceptor (rf/after (partial check-and-throw :igloo.db/db)))

(def db->local-store (rf/after db/db->local-store))
