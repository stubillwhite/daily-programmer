(ns daily-programmer.texas-holdem.game
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [daily-programmer.texas-holdem.analysis :as analysis]
    [daily-programmer.texas-holdem.ui :as ui]
    [daily-programmer.texas-holdem.ai :as ai]
    [daily-programmer.texas-holdem.data-model :as dm]
    [taoensso.timbre :as timbre])
  (:use
    [daily-programmer.utils :refer [def-]]))

(timbre/refer-timbre)

(defn- deal-n-cards-to
  ([hand target n]
    (-> hand
      (update-in target  (fn [x] (concat (take n (hand :deck)) x)))
      (update-in [:deck] (fn [x] (drop n x))))))

(defn- deal-to-players
  ([hand]
    (reduce
      (fn [hand id] (deal-n-cards-to hand [:hands-of-cards id :hole-cards] 1))
      hand
      (keys (hand :players)))))

(defn- burn-card  [hand] (deal-n-cards-to hand [:common-cards :discards] 1))
(defn- deal-flop  [hand] (deal-n-cards-to hand [:common-cards :flop]     3))
(defn- deal-turn  [hand] (deal-n-cards-to hand [:common-cards :turn]     1))
(defn- deal-river [hand] (deal-n-cards-to hand [:common-cards :river]    1))

(defn- get-player-action
  ([hand id action-phase]
    (let [action-func (get-in hand [:players id :action-func])]
      (action-func hand action-phase))))

;; TODO: Implement folding
(defn- execute-player-action
  ([hand id action-phase action]
    (assoc-in hand [:player-actions id action-phase] action)))

(defn- perform-player-action
  ([hand id action-phase]
    (let [action (get-player-action hand id action-phase)]
      (-> hand
        (ui/display-player-action id action)
        (execute-player-action id action-phase action)))))

(defn player-actions
  ([{:keys [players] :as hand} action-phase]
    (reduce
      (fn [hand id] (perform-player-action hand id action-phase))
      hand
      (keys players))))

(defn play-hand
  ([hand]
    (-> hand
      (deal-to-players)
      (deal-to-players)

      (ui/display-player-hands)
      (player-actions :before-the-flop)

      (burn-card)
      (deal-flop)
      (ui/display-flop)
      (player-actions :after-the-flop)

      (burn-card)
      (deal-turn)
      (ui/display-turn)
      (player-actions :after-the-turn)

      (burn-card)
      (deal-river)
      (ui/display-river)
      (player-actions :after-the-river)
      
      (analysis/calculate-best-hand-per-player)
      (analysis/calculate-player-hand-rank)

      (ui/display-final-hand-state))))

(defn play-hand-of-game
  ([game]
    (let [hand-of-game (dm/hand-of-game (game :players) (game :deck))]
      (play-hand hand-of-game))))

(defn play-example-game
  ([]
    (let [ player-ids (for [x (range 1 5)] (str "P" x))
           players    (for [id player-ids] (dm/player id (partial ai/fold-if-no-face-cards id)))
           game       (dm/game players deck/standard-deck)]
      (->  game
        (play-hand-of-game)))))
