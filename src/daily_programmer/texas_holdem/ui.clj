(ns daily-programmer.texas-holdem.ui
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [daily-programmer.texas-holdem.data-model :as dm]
    [clojure.string :as string]
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- cards-to-str
  ([cards]
    (string/join " "
      (map
        (fn [{:keys [suit value]}] (format "%s%s" (get deck/value-strs value) (get deck/suit-strs suit)))
        cards))))

(defn display-player-hands
  ([{:keys [hands-of-cards] :as hand}]
    (let [ player-cards (fn [id] (format "%s cards: %s" id
                                  (cards-to-str (get-in hands-of-cards [id :hole-cards])))) ]
      (println (format "%s" (string/join "\n" (map player-cards (keys hands-of-cards)))))
      hand)))

(defn display-flop
  ([hand]
    (println (format "\nFlop:   %s" (cards-to-str (get-in hand [:common-cards :flop]))))
    hand))

(defn display-turn
  ([hand]
    (println (format "\nTurn:   %s" (cards-to-str (get-in hand [:common-cards :turn]))))
    hand))

(defn display-river
  ([hand]
    (println (format "\nRiver:  %s" (cards-to-str (get-in hand [:common-cards :river]))))
    hand))

(defn- action-to-str
  ([action]
    (get { :check "Check"
           :fold  "Fold"
           :raise "Raise" } action)))

(defn display-player-action
  ([hand id action]
    (println (format "%s: %s" id (action-to-str action)))
    hand))

(defn- player-best-hand-to-str
  ([hands-of-cards id]
    (format "%s best hand: %s (%s)" id
      (cards-to-str (get-in hands-of-cards [id :best-hand :hand]))
      (get-in hands-of-cards [id :best-hand :description]))))

(defn display-final-hand-state
  ([{:keys [hands-of-cards player-rank] :as hand}]
    (println
      (format "\nFinal standings:\n%s"
        (string/join "\n" (map (fn [x] (player-best-hand-to-str hands-of-cards x)) player-rank))))
    hand))
