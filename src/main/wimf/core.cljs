(ns wimf.core
  (:require [reagent.dom :as dom]
            [wimf.views :as views]
            [wimf.db :as db]
            [wimf.events]
            [wimf.subs]
            [re-frame.core :as rf]))

(enable-console-print!) 

(defn app
  []
  [views/main])

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (dom/render [app]
              (.getElementById js/document "app")))

(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (rf/dispatch-sync [:app/initialize])
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
