(ns exercise-data-parser.core
  (:gen-class)
  (:require [clojure-csv.core :refer [parse-csv]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clojure.string :refer [split]]
            [clojure.data.json :as json]))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(let [csv-str (slurp "/exercise-data-parser/resources/exercise-data-2016.csv")
      csv-vec (parse-csv csv-str)
      workouts (->> csv-vec
                   first
                   (drop 3)
                   vec)]
  workouts)


