(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
    [clojure.java.io :as io]
    [clojure.java.javadoc :refer (javadoc)]
    [clojure.pprint :refer (pprint)]
    [clojure.reflect :refer (reflect)]
    [clojure.repl :refer (apropos dir doc find-doc pst source)]
    [clojure.set :as set]
    [clojure.string :as str]
    [clojure.stacktrace :refer (print-stack-trace)]
    [clojure.test :as test]
    [clojure.tools.namespace.repl :refer (refresh refresh-all)]
    [clojure.tools.trace :refer (trace-ns untrace-ns)]
    [daily-programmer.system :as system]
    [daily-programmer.core :as core]
    [daily-programmer.texas-holdem.game :as game]
    [daily-programmer.texas-holdem.analysis :as analysis])
  (:use
    [midje.repl]))

(def system
  "A Var containing an object representing the application under development."
  nil)

(defn init
  "Creates and initializes the system under development in the Var #'system."
  []
  (alter-var-root #'system
    (constantly (system/system))))

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (alter-var-root #'system system/start))

(defn stop
  "Stops the system if it is currently running, updates the Var #'system."
  []
  (alter-var-root #'system
    (fn [s] (when s (system/stop s)))))

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))



(pprint
  (let [ players (for [x (range 1 6)] (game/player (str "P" x)))
         game    (game/play-game (game/game players))
         hands   (into {} (for [ p (vals (game :players)) ] [ (-> p :best-hand :cards) (p :id) ]))
         winners (analysis/rank-hands (keys hands)) ]
    (map (fn [x] (get hands (x :cards))) winners)))




















