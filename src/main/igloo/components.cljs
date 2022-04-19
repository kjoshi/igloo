(ns igloo.components)

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
            :type (or type "button")
            :on-click on-click}
   (or title "Save")])


