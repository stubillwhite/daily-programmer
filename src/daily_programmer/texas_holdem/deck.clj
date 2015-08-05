(ns daily-programmer.texas-holdem.deck
  (:require
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def suits  [ :clubs :diamonds :hearts :spades ])
(def values [ 2 3 4 5 6 7 8 9 10 11 12 13 14 ])

(def standard-deck
  (for [ s suits v values ] { :suit s :value v }))

(def suit-strs  (zipmap suits  ["\u2663" "\u2666" "\u2665" "\u2660"]))
(def value-strs (zipmap values ["2" "3" "4" "5" "6" "7" "8" "9" "10" "J" "Q" "K" "A"]))
