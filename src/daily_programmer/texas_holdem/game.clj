(ns daily-programmer.texas-holdem.game
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [daily-programmer.texas-holdem.action :as action]
    [daily-programmer.texas-holdem.analysis :as analysis]
    [daily-programmer.texas-holdem.ui :as ui]
    [daily-programmer.texas-holdem.ai :as ai]
    [taoensso.timbre :as timbre])
  (:use
    [daily-programmer.texas-holdem.data-model :refer [hand-of-cards hand-of-game player game]]
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
      (fn [hand id] (-> hand (deal-card-to [:hands-of-cards id :hole-cards])))
      hand
      (keys (hand :players)))))

(defn perform-player-action
  ([hand id phase]
    (let [action-func (get-in hand [:players id :action-func])]
      (action-func phase hand))))

(defn perform-player-action
  ([hand id phase]
    hand))

(defn player-actions
  ([{:keys [players]} phase]
    (reduce
      (fn [hand id] (perform-player-action hand id phase))
      (keys players))))

(defn play-hand
  ([hand]
    (-> hand
      (deal-to-players)
      (deal-to-players)
;;      (player-actions ::pre-flop)
      (burn-card)
      (deal-flop)
;;      (player-actions ::flop)
      (burn-card)
      (deal-turn)
;;      (player-actions ::turn)
      (burn-card)
      (deal-river)
;;      (player-actions ::river)
      (analysis/calculate-best-hand-per-player)
      (analysis/calculate-player-rank)
      (ui/display-hand)
      )))

(defn play-hand-of-game
  ([game]
    (let [hand-of-game (hand-of-game (game :players) (game :deck))]
      (play-hand hand-of-game))))

(defn play-example-game
  ([]
    (let [ players (for [x (range 1 11)] (player (str "P" x) ai/always-check))
           game    (game players deck/standard-deck)]
      (->  game
        (play-hand-of-game)))))
