(ns igloo.db
  (:require [cljs.reader]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(s/def ::id int?)
(s/def ::section-ids (s/coll-of int? :kind vector? :distinct true))
(s/def ::name string?)
(s/def ::quantity integer?)
(s/def ::created string?)
(s/def ::item (s/keys :req-un [::id ::name ::quantity ::created]
                      :opt-un [::section-ids]))
(s/def ::items (s/and
                (s/map-of ::id ::item)
                #(instance? PersistentTreeMap %)))

(s/def ::section (s/keys :req-un [::id ::name ::colour]))
(s/def ::freezer (s/and
                  (s/map-of ::id ::section)
                  #(instance? PersistentTreeMap %)))

(s/def ::active-mode #{:new :edit :inactive})
(s/def ::form (s/keys :req-un [::active-mode]))
(s/def ::config-form (s/keys :req-un [::active-mode]))

(s/def ::sort-key #{:name :created :quantity})
(s/def ::reverse-sort? boolean?)

(s/def ::show-locations? boolean?)

(s/def ::announcement-id int?)
(s/def ::announcement-id-seen int?)
(s/def ::announcement-visible? boolean?)

(s/def ::db (s/keys :req-un [::items ::sort-key ::reverse-sort? ::form ::config-form ::freezer ::show-locations? ::announcement-id ::announcement-id-seen ::announcement-visible?]))

(def ls-key "wimf-items")                         ;; localstore key

(defn items->local-store
  "Puts items into localStorage"
  [items]
  (.setItem js/localStorage ls-key (str items)))

(rf/reg-cofx
 :local-store-items
 (fn [cofx _]
   (assoc cofx :local-store-items
          (into (sorted-map)
                (some->> (.getItem js/localStorage ls-key)
                         (cljs.reader/read-string))))))

(def db-key-v1 "igloo-db.v1")
(defn db->local-store
  "Puts app-db into localStorage"
  [db]
  (.setItem js/localStorage db-key-v1 (str db)))

(rf/reg-cofx
 :local-store-db
 (fn [cofx _]
   (let [local-store-map (some->> (.getItem js/localStorage db-key-v1)
                                  (cljs.reader/read-string))
         parsed-map (cond-> local-store-map
                      (:items local-store-map)
                      (assoc :items (into (sorted-map) (:items local-store-map)))
                      (:freezer local-store-map)
                      (assoc :freezer (into (sorted-map) (:freezer local-store-map))))]
     (assoc cofx :local-store-db parsed-map))))

(def default-items
  {1 {:id 1
      :name "Tomato bolognese with mushrooms"
      :quantity 1
      :created "2022-04-05"
      :section-ids []}
   2  {:id 2
       :name "Tomato soup"
       :quantity 3
       :created "2022-04-02"
       :section-ids []}})

(def default-freezer
  {1 {:id 1
      :name "Top drawer"
      :colour "#048ba8"}
   2 {:id 2
      :name "Middle drawer"
      :colour "#ff715b"}
   3 {:id 3
      :name "Bottom drawer"
      :colour "#99c24d"}})

(def default-db
  {:items (into (sorted-map) default-items)
   :sort-key :created
   :reverse-sort? false
   :form {:active-mode :inactive}
   :config-form {:active-mode :inactive}
   :show-locations? true
   :announcement-id 1
   :announcement-id-seen 0
   :announcement-visible? false
   :freezer (into (sorted-map) default-freezer)})

