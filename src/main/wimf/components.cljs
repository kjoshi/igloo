(ns wimf.components
  (:require [fork.re-frame :as fork]
            [re-frame.core :as rf]
            [wimf.util :refer [current-date]]
            [vlad.core :as vlad]))

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
       (fn [{:keys [form-id handle-submit values handle-change handle-blur submitting? errors touched]}]
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

          [:button {:class "text-gray-900 border border-2 rounded-xl border-current py-2 text-xl font-medium w-full hover:bg-gray-200"
                    :type "submit"
                    :disabled submitting?}
           "Save"]
          ]
         )])))

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

