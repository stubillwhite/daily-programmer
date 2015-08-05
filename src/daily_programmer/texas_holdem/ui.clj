(ns daily-programmer.texas-holdem.ui
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [clojure.string :as string]
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- cards-to-str
  ([cards]
    (string/join " "
      (map
        (fn [{:keys [suit value]}] (format "%s%s" (get deck/value-strs value) (get deck/suit-strs suit)))
        cards))))

(defn- hand-to-str
  ([{:keys [hands-of-cards flop turn river player-rank] :as hand}]
    (let [ player-cards     (fn [id] (format "%s cards: %s" id
                                      (cards-to-str (get-in hands-of-cards [id :hole-cards]))))
           
           player-best-hand (fn [id] (format "%s best hand: %s (%s)" id
                                      (cards-to-str (get-in hands-of-cards [id :best-hand :hand]))
                                      (get-in hands-of-cards [id :best-hand :description]))) ]
      (str
        (string/join "\n" (map player-cards (keys hands-of-cards)))
        (format "\nFlop:  %s" (cards-to-str (get-in hand [:flop])))
        (format "\nTurn:  %s" (cards-to-str (get-in hand [:turn])))
        (format "\nRiver: %s" (cards-to-str (get-in hand [:river])))
        "\n--\n"
        (string/join "\n" (map player-best-hand player-rank))))))

(defn display-hand
  ([hand]
    (print (hand-to-str hand))
    hand))
