(ns daily-programmer.texas-holdem.game
  (:require
    [clojure.string :as string]
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def suits  [ :clubs :diamonds :hearts :spades ])
(def values [ :two :three :four :five :six :seven :eight :nine :ten :jack :queen :king :ace ])

(def standard-deck
  (for [ s suits v values ] { :suit s :value v }))

(defn player
  ([id]
    { :id    id
      :cards [] }))

(defn game
  ([players]
    { :players  (into {} (for [p players] [(p :id) p]))
      :deck     (shuffle standard-deck)
      :discards []
      :flop     []
      :turn     []
      :river    [] }))

(defn deal-card-to
  ([game target]
    (-> game
      (update-in target  (fn [x] (cons (first (game :deck)) x)))
      (update-in [:deck] (fn [x] (drop 1 x))))))

(defn burn-card
  ([game]
    (-> game
      (deal-card-to [:discards]))))

(defn deal-flop
  ([game]
    (-> game
      (deal-card-to [:flop])
      (deal-card-to [:flop])
      (deal-card-to [:flop]))))

(defn deal-turn
  ([game]
    (-> game
      (deal-card-to [:turn]))))

(defn deal-river
  ([game]
    (-> game
      (deal-card-to [:river]))))

(defn deal-to-players
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
      (deal-river))))

(def suit-strs  (zipmap suits  ["\u2663" "\u2666" "\u2665" "\u2660"]))
(def value-strs (zipmap values ["2" "3" "4" "5" "6" "7" "8" "9" "10" "J" "Q" "K" "A"]))

(defn hand-to-str
  ([cards]
    (string/join " "
      (map
        (fn [{:keys [suit value]}] (format "%s%s" (get value-strs value) (get suit-strs suit)))
        cards))))

(defn game-to-str
  ([{:keys [players flop turn river] :as game}]
    (str
      (string/join "\n"
        (map
          (fn [x] (format "Player %s hand: %s" x (hand-to-str (get-in game [:players x :cards]))))
          (keys players)))
      (format "\nFlop:  %s" (hand-to-str (get-in game [:flop])))
      (format "\nTurn:  %s" (hand-to-str (get-in game [:turn])))
      (format "\nRiver: %s" (hand-to-str (get-in game [:river]))))))

(defn play-example-game
  ([]
    (let [ players (for [x (range 1 9)] (player x))
           game    (game players) ]
      (print (game-to-str (play-game game))))))



