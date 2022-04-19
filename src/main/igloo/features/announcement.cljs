(ns igloo.features.announcement
  (:require [re-frame.core :as rf]
            [igloo.features.common :as common]))

;;
;; Subscriptions
(rf/reg-sub
 :announcement/latest-id
 :-> :announcement-id)

(rf/reg-sub
 :announcement/seen-id
 :-> :announcement-id-seen)

(rf/reg-sub
 :announcement/latest-seen?
 :<- :announcement/latest-id
 :<- :announcement/seen-id
 (fn [[current-id seen-id] _]
   (= current-id seen-id)))

(rf/reg-sub
 :announcement/visible?
 :-> :announcement-visible?)

;;
;; Events
(rf/reg-event-db
 :announcement/toggle
 [common/check-spec-interceptor
  common/db->local-store]
 (fn [db _]
   (update db :announcement-visible? not)))

(rf/reg-event-db
  :announcement/set-seen-latest
 [common/check-spec-interceptor
  common/db->local-store]
(fn [db _]
  (assoc db :announcement-id-seen (:announcement-id db))) )

;;
;; Views



