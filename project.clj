(defproject idx-data "0.1.0-SNAPSHOT"
  :description "IDX Data transformer"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.csv "0.1.3"]
                 [clj-http "2.0.0"]
                 [clj-time "0.11.0"]]
  :main ^:skip-aot idx-data.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
