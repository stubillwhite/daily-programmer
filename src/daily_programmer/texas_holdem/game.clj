(ns daily-programmer.texas-holdem.game
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [daily-programmer.texas-holdem.analysis :as analysis]
    [daily-programmer.texas-holdem.ui :as ui]
    [taoensso.timbre :as timbre])
  (:use
    [daily-programmer.texas-holdem.data-model :refer [hand-player hand game-player game]]
    [daily-programmer.utils :refer [def-]]))

(timbre/refer-timbre)

(defn- deal-card-to
  ([game target]
    (-> game
      (update-in target  (fn [x] (cons (first (game :deck)) x)))
      (update-in [:deck] (fn [x] (drop 1 x))))))

(defn- burn-card
  ([game]
    (-> game
      (deal-card-to [:discards]))))

(defn- deal-flop
  ([hand]
    (-> hand
      (deal-card-to [:flop])
      (deal-card-to [:flop])
      (deal-card-to [:flop]))))

(defn- deal-turn
  ([hand]
    (-> hand
      (deal-card-to [:turn]))))

(defn- deal-river
  ([hand]
    (-> hand
      (deal-card-to [:river]))))

(defn- deal-to-players
  ([hand]
    (reduce
      (fn [hand id] (-> hand (deal-card-to [:players id :cards])))
      hand
      (keys (hand :players)))))

(defn play-hand
  ([hand]
    (-> hand
      (deal-to-players)
      (deal-to-players)
      (burn-card)
      (deal-flop)
      (burn-card)
      (deal-turn)
      (burn-card)
      (deal-river)
      (analysis/calculate-best-hand-per-player)
      (analysis/calculate-player-rank)
      (ui/display-hand))))

(defn play-hand-of-game
  ([game]
    (-> game
      (assoc-in  [:hand] (hand (game :players) (game :deck)))
      (update-in [:hand] play-hand))))

(defn play-example-game
  ([]
    (let [ players (for [x (range 1 11)] (game-player (str "P" x)))
           game    (game players deck/standard-deck) ]
      (-> game
        (play-hand-of-game)))))
