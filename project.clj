(defproject daily-programmer "0.1.0-SNAPSHOT"

  :description "TODO"

  :url "TODO"

  :license { :name "Eclipse Public License"
             :url "http://www.eclipse.org/legal/epl-v10.html" }

  :repl-options { :port 4555 }

  :plugins [ [lein-expectations "0.0.7"]
             [lein-autoexpect "1.4.0"] ]

  
  :dependencies [
                  ;; Core
                  [org.clojure/clojure "1.6.0"]
                  
                  ;; Logging
                  [com.taoensso/timbre "3.3.1"]

                  ;; Debugging
                  [org.clojure/tools.trace "0.7.8"]
                  
                  [midje "1.6.3"] ]
  
  :profiles { :dev { :dependencies [ [org.clojure/tools.namespace "0.2.10"]
                                     [expectations "2.1.0"] ]
                     :source-paths ["dev"] } })
