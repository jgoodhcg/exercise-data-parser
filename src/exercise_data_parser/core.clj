(ns exercise-data-parser.core
  (:gen-class)
  (:require [clojure-csv.core :refer [parse-csv]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clojure.string :refer [split join]]
            [clojure.data.json :as json]))

(defn get-workout-names [csv-vec]
  (->> csv-vec
       first
       (drop 3)
       (map-indexed (fn [index workout-name]
                      {index
                       (as-> workout-name name-str
                         (split name-str #"\s")
                         (join #"-" name-str)
                         (keyword name-str))}))
       (reduce conj)))

(defn parse-individual-workout [workouts row-num index srw]
  (try
    (if (not= srw "")
      {(get workouts index)
       (as-> srw srw-str
         (split srw-str #"\|\|")
         (map (fn [s] (split s #";")) srw-str)
         (map (fn [s] {:sets (nth s 0)
                       :reps (nth s 1)
                       :weight (nth s 2)}) srw-str)
         (vec srw-str))})
    (catch Exception e
      (pprint "--------")
      (pprint e)
      (pprint {:row row-num :index index :srw srw})
      nil)))

(defn parse-workouts [workout-raw workouts row-num]
  (->> workout-raw
       (map-indexed (partial parse-individual-workout workouts row-num))
       (filter some?)))

(defn parse-individual-day [workouts index entry]
  (try
    (let [date (split (nth entry 0) #"/") ;; [month day year]
          ;; [hour minute] - 0-23 hour
          start-time (split (nth entry 1) #":")
          stop-time (split (nth entry 2) #":")
          year (Integer/parseInt (nth date 2))
          month (Integer/parseInt (nth date 0))
          day (Integer/parseInt (nth date 1))
          start-hour (Integer/parseInt (nth start-time 0))
          start-minute (Integer/parseInt (nth start-time 1))
          stop-hour (Integer/parseInt (nth stop-time 0))
          stop-minute (Integer/parseInt (nth stop-time 1))

          start (time/from-time-zone
                 (time/date-time
                  year month day start-hour start-minute)
                 (time/time-zone-for-offset -5))
          stop (time/from-time-zone
                (time/date-time
                 year month day stop-hour stop-minute)
                (time/time-zone-for-offset -5))

          workout-raw (drop 3 entry)
          data (parse-workouts workout-raw workouts index)]

      {:start (coerce/to-long start)
       :stop  (coerce/to-long stop)
       :data  data})
    (catch Exception e
      (pprint "--------")
      (pprint "--------")
      (pprint e)
      (print entry)
      nil)))

(defn parse-days [raw-data workouts]
  (->> raw-data
       (map-indexed (partial parse-individual-day workouts))
       (filter some?)))

(defn filter-groups [entry]
  (update-in entry [:data]
             (fn [old]
               {:runs (into (sorted-map)
                            (filter
                             #(or
                               (contains? % :runs-indoor-track)
                               (contains? % :runs-outdoor-track)
                               (contains? % :runs-street))
                             old))
                :exercises (into (sorted-map)
                                 (filter
                                  #(and
                                    (not (contains? % :runs-indoor-track))
                                    (not (contains? % :runs-outdoor-track))
                                    (not (contains? % :runs-street)))
                                  old))})))

(defn -main
  [path]
  (println (str "Parsing " path))
  (let [csv-str  (slurp path)
        csv-vec  (parse-csv csv-str)
        workouts (get-workout-names csv-vec) ;; {index :workout-name}
        raw-data (rest csv-vec)

        ;; [{:start unix-time
        ;;   :stop unix-time
        ;;   :data [{:workout-name [{:sets 1 :reps 2 :weight 3}]}]}]
        data     (parse-days raw-data workouts)
        data-filtered (map filter-groups data)]

    ;; (pprint (filter #(not (empty? (get-in % [:data :runs])))
    ;;                 data-filtered ))
    (spit (str path ".json") (json/write-str data-filtered))
  ))
