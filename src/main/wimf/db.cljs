(ns wimf.db
  (:require [cljs.reader]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(s/def ::id int?)
(s/def ::name string?)
(s/def ::quantity integer?)
(s/def ::created string?)
(s/def ::item (s/keys :req-un [::id ::name ::quantity ::created]))
(s/def ::items (s/and
                (s/map-of ::id ::item)
                #(instance? PersistentTreeMap %)))

(s/def ::active-mode #{:new :edit})
(s/def ::form (s/keys :opt-un [::id ::name ::quantity ::created ::active-mode]))

(s/def ::sort-key #{:name :created :quantity})
(s/def ::reverse-sort? boolean?)

(s/def ::db (s/keys :req-un [::items ::sort-key ::reverse-sort? ::form]))

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

(def default-items
  {1 {:id 1
      :name "Tomato bolognese with mushrooms"
      :quantity 1
      :created "2022-04-05"}
   2  {:id 2
       :name "Daal with spinach"
       :quantity 3
       :created "2022-04-02"}})

(def default-db
  {:items (into (sorted-map) default-items)
   :sort-key :created
   :reverse-sort? false
   :form {}})

(comment

  default-db
  (s/explain ::db default-db)

  ;
) ; nil
