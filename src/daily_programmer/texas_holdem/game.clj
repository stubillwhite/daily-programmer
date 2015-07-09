(ns daily-programmer.texas-holdem.game
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [daily-programmer.texas-holdem.analysis :as analysis]
    [clojure.string :as string]
    [taoensso.timbre :as timbre])
  (:use
    [daily-programmer.utils :refer [def-]]))

(timbre/refer-timbre)

(defn player
  "Returns a new player."
  ([id]
    { :id    id
      :cards [] }))

(defn game
  "Returns a new game."
  ([players]
    { :players  (into {} (for [p players] [(p :id) p]))
      :deck     (shuffle deck/standard-deck)
      :discards []
      :flop     []
      :turn     []
      :river    [] }))

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
  ([game]
    (-> game
      (deal-card-to [:flop])
      (deal-card-to [:flop])
      (deal-card-to [:flop]))))

(defn- deal-turn
  ([game]
    (-> game
      (deal-card-to [:turn]))))

(defn- deal-river
  ([game]
    (-> game
      (deal-card-to [:river]))))

(defn- deal-to-players
  ([game]
    (reduce
      (fn [game id] (-> game (deal-card-to [:players id :cards])))
      game
      (keys (game :players)))))

(defn play-game
  ([game]
    (-> game
      (deal-to-players)
      (deal-to-players)
      (burn-card)
      (deal-flop)
      (burn-card)
      (deal-turn)
      (burn-card)
      (deal-river)
      (analysis/calculate-best-hand-per-player)
      (analysis/calculate-player-rank))))

(defn- cards-to-str
  ([cards]
    (string/join " "
      (map
        (fn [{:keys [suit value]}] (format "%s%s" (get deck/value-strs value) (get deck/suit-strs suit)))
        cards))))

(defn game-to-str
  ([{:keys [players flop turn river player-rank] :as game}]
    (let [ player-cards     (fn [id] (format "%s cards: %s" id
                                      (cards-to-str (get-in players [id :cards]))))
           
           player-best-hand (fn [id] (format "%s best hand: %s (%s)" id
                                      (cards-to-str (get-in players [id :best-hand :cards]))
                                      (get-in players [id :best-hand :description]))) ]
      (str
        (string/join "\n" (map player-cards (keys players)))
        (format "\nFlop:  %s" (cards-to-str (get-in game [:flop])))
        (format "\nTurn:  %s" (cards-to-str (get-in game [:turn])))
        (format "\nRiver: %s" (cards-to-str (get-in game [:river])))
        "\n--\n"
        (string/join "\n" (map player-best-hand player-rank))))))

(defn play-example-game
  ([]
    (let [ players (for [x (range 1 6)] (player (str "P" x)))
           game    (game players) ]
      (print (game-to-str (play-game game))))))
