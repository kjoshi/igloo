(ns wimf.components
  (:require [fork.re-frame :as fork]
            [re-frame.core :as rf]
            [wimf.util :refer [current-date]]
            [vlad.core :as vlad]))

(defn icon-button [svg-path-d {:keys [size-class on-click]}]
  [:button {:class ["border" "border-2" "rounded-xl" "border-current" "text-current" "p-2" "font-medium" "hover:bg-gray-200"]
            :type "button"
            :on-click on-click}
   [:svg {:xmlns "http://www.w3.org/2000/svg", :class (or size-class "h-8 w-8"), :fill "none", :viewBox "0 0 24 24", :stroke "currentColor", :stroke-width "2"}
    [:path {:stroke-linecap "round", :stroke-linejoin "round", :d svg-path-d}]]])

(defn close-form-button [on-click]
  [:div.text-gray-900
   [icon-button  "M6 18L18 6M6 6l12 12"
    {:on-click on-click}]])

(defn button [{:keys [type on-click title text-colour bg-colour hover-colour width text-size font-weight border-width border-colour]}]
  [:button {:class ["border" "rounded-xl" "py-2"
                    (or border-colour "border-current")
                    (or border-width "border-2")
                    (or font-weight "font-medium")
                    (or text-size "text-xl")
                    (or width "w-full")
                    (or text-colour "text-gray-900")
                    (or bg-colour "bg-white")
                    (or hover-colour "hover:bg-gray-200")]
            :type type
            :on-click on-click}
   (or title "Save")])

(def form-validation
  (vlad/join (vlad/attr [:name] (vlad/present))
             (vlad/attr [:quantity] (vlad/present))
             (vlad/attr [:created] (vlad/present))))

(defn fork-form []
  (let [fields (rf/subscribe [:form/fields])
        css "mt-2 block w-full rounded-md bg-gray-200 border-transparent focus:border-gray-500 focus:bg-white focus:ring-0"]
    (fn []
      [fork/form
       {:path [:fork-form]
        :form-id "item-form"
        :prevent-default? true
        :clean-on-unmount? true
        :keywordize-keys true
        :initial-values {:id (:id @fields)
                         :name (:name @fields)
                         :quantity (str  (:quantity @fields))
                         :created (or (:created @fields) (current-date))}
        :validation #(vlad/field-errors form-validation %)
        :on-submit #(rf/dispatch [:form/submit %])}
       (fn [{:keys [form-id handle-submit values handle-change handle-blur errors touched]}]
         [:form {:class "grid grid-cols-1 gap-3 mt-8 grow-0"
                 :id form-id
                 :on-submit handle-submit}
          [:label.block
           [:div.text-gray-900.text-xl.font-medium "Name"]
           [:input {:type "text"
                    :class css
                    :name :name
                    :value (values :name)
                    :on-change handle-change
                    :on-blur handle-blur}]
           [:div.h-6.text-red-500
            (when (touched :name) (first (get errors (list :name))))]]
          [:label.block
           [:div.text-gray-900.text-xl.font-medium "How many"]
           [:input {:type "number"
                    :class css
                    :name :quantity
                    :value (values :quantity)
                    :on-change handle-change
                    :on-blur handle-blur}]
           [:div.h-6.text-red-500
            (when (touched :quantity) (first (get errors (list :quantity))))]]
          [:label.block
           [:div.text-gray-900.text-xl.font-medium "Cooked date"]
           [:input {:type "date"
                    :class css
                    :name :created
                    :value (values :created)
                    :on-change handle-change
                    :on-blur handle-blur}]
           [:div.h-6.text-gray-400
            (when (touched :created) [:span.text-red-500 (first (get errors (list :created)))])]]

          [button {:type "submit" :title "Save"}]])])))

(comment
  (def errors (vlad/field-errors form-validation {:test 4}))
  errors
  ; {(:name) ["Name is required."], (:quantity) ["Quantity is required."]}

  (first (get errors '("name")))

  (def f {:name "Tomato bolognese with mushrooms" , :quantity 1 , :created "2022-04-05"})
  f
; {:name "Tomato bolognese with mushrooms",
;  :quantity 1,
;  :created "2022-04-05"}
  (vlad/field-errors form-validation f)
  (str nil)

  ;
  )

