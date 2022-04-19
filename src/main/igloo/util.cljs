(ns igloo.util)

(def months ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])

(defn current-date []
  (subs (.toISOString (js/Date.)) 0 10))

(defn date-string
  "Takes a date string in the format YYYY-mm-dd.
  Returns a date string like: 24 Feb"
  [date-str]
  (let [[_ month day] (re-seq #"\d+" date-str)
        month-str (nth months (dec (js/parseInt month)))
        day-str (str (js/parseInt day))]
    (str day-str " " month-str)))

(comment
  (date-string (js/Date.))

  (.parse js/Date "2022-04-02")

  (re-seq #"\d+" "2022-04-02")

  (date-string "2022-03-23")

  (current-date)
  ;
  )
