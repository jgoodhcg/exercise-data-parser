(ns exercise-data-parser.core
  (:gen-class)
  (:require [clojure-csv.core :refer [parse-csv]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clojure.string :refer [split join]]
            [clojure.data.json :as json]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; [{:start unix-time :stop unix-time :workout-name {:sets 1 :reps 2 :weight 3}}]
(let [csv-str (slurp "/exercise-data-parser/resources/exercise-data-2016.csv")
      csv-vec (parse-csv csv-str)
      workouts (vec (->> csv-vec ;; [{:workout-name col-num}]
                         first
                         (drop 3)
                         (map-indexed (fn [index workout-name]
                                        {(as-> workout-name name-str
                                           (split name-str #"\s")
                                           (join #"-" name-str)
                                           (keyword name-str))
                                         index}))))
      raw-data (rest csv-vec)
      data (->> raw-data
                (map (fn [entry]
                       (let [date (split (nth entry 0) #"/") ;; [month day year]
                             ;; [hour minute] - 0-23 hour
                             start-time (split (nth entry 1) #":")
                             stop-time (split (nth entry 2) #":")
                             year (Integer/parseInt (nth date 2))
                             month (Integer/parseInt (nth date 0))
                             day (Integer/parseInt (nth date 1))
                             start-hour (Integer/parseInt (nth start-time 0))
                             start-minute (Integer/parseInt (nth start-time 1))
                             stop-hour (Integer/parseInt (nth start-time 0))
                             stop-minute (Integer/parseInt (nth start-time 1))

                             start (time/from-time-zone
                                    (time/date-time
                                     year month day start-hour start-minute)
                                    (time/time-zone-for-offset -5))
                             stop (time/from-time-zone
                                    (time/date-time
                                     year month day stop-hour stop-minute)
                                    (time/time-zone-for-offset -5))
                             ]

                         {:start (coerce/to-long start)
                          :stop  (coerce/to-long stop)}
                         ))))
      ]
  (take 4 data))


