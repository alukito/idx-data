(ns idx-data.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-time.format :as datefmt])
  (:use [clojure.core.async :only [chan go close! >! >!! <! <!!]])
  (:gen-class))

(def quandl-url "https://www.quandl.com/api/v3/datasets/")
(def API_KEY "Vmqy9sMEGuPWRWAyjdrw")

(defn wrap-api-key
  [url]
  (if (some #(= \? %) url)
    (str url "&api_key=" API_KEY)
    (str url "?api_key=" API_KEY)))

(defn quandl->json
  [code start-date]
     (println (str "Processing " code))
     (json/read-str
      (:body (client/get
              (wrap-api-key
               (str quandl-url code ".json?start_date=" start-date)) {:as :json}))))


(defn json->csv
  [dataset-json]
  (let [dataset (get dataset-json "dataset")
        data (get dataset "data")
        code (get dataset "dataset_code")]
    (map #(conj % code) data)))

(def yyyyMMdd (datefmt/formatter "yyyy-MM-dd"))
(def MMddyyyy (datefmt/formatter "M/d/yyyy"))

(defn- dateformat
  [date]
  (datefmt/unparse MMddyyyy (datefmt/parse yyyyMMdd date)))


;<date>,<ticker>,<open>,<high>,<low>,<close>,<volume>
;2/27/2015,AALI,25000,25375,24650,24650,1396600
;2/27/2015,ABBA,63,64,60,60,22100
(defn metastock-format
  [data]
  (map (fn [line]
         (concat
          [(dateformat (line 0)) (subs (line 7) 3)]
          (map #(format "%.0f" (line %)) (range 1 6))))
       data))

(defn make-csv!
  [codes date]
  (let [c (chan 10)]
    (go
     (doseq [code codes]
       (>! c (->>
              (quandl->json code date)
              (json->csv)
              (metastock-format))))
     (close! c))

     (with-open [out-file (io/writer (str date ".csv"))]
       (csv/write-csv out-file [["<date>" "<ticker>" "<open>" "<high>" "<low>" "<close>" "<volume>"]])
       (loop []
         (when-let [val (<!! c)]
           (csv/write-csv out-file val)
           (recur))))))

(defn jk-codes
  [url]
  (->>
   (client/get url)
   (:body)
   (csv/read-csv)
   (drop 1)
   (map #(% 1))))

(defn -main
  "I don't do a whole lot ... yet."
  [date]
  (->
   (jk-codes "https://s3.amazonaws.com/quandl-static-content/Ticker+CSV%27s/Yahoo/JK.csv")
   (make-csv! date)
   (time)))

