(ns daily-programmer.texas-holdem.ai
  (:require
    [daily-programmer.texas-holdem.action :as action]))

(defn always-check
  ([game phase]
    action/check))

