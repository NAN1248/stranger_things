(defproject adventure "0.1.0-SNAPSHOT"
  :description "Our Group's CS296, Stranger Things themed Clojure, text-based adventure game"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
								 [org.clojars.beppu/clj-audio "0.3.0"]]
  :main ^:skip-aot adventure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
