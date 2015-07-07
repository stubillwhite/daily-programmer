(ns daily-programmer.texas-holdem.analysis
  (:require
    [clojure.string :as string]
    [daily-programmer.texas-holdem.deck :as deck]
    [taoensso.timbre :as timbre])
  (:use
    [daily-programmer.utils :refer [def-]]))

(timbre/refer-timbre)

;; If we were doing this for real then we'd be more concerned about execution time and would use a pre-computed table of
;; hand to value. But this is just for fun and we don't care about execution time so we'll calculate the value on demand
;; and will arrange for the cards to be sorted by value (again, costs time) before being analysed.

;; Predicates to identify types of hand

(defn- flush?
  ([cards]
    (apply = (map :suit cards))))

(defn- straight?
  ([cards]
    (let [ values   (map :value cards)
           low-card (first values)
           expected (take (count values) (drop-while (fn [x] (not= x low-card)) deck/values)) ]
      (= values expected))))

(defn- straight-flush?
  ([cards]
    (and (straight? cards) (flush? cards))))

(defn- royal-flush?
  ([cards]
    (and (straight-flush? cards) (= 14 (:value (last cards))))))

(defn- group-by-value
  ([cards]
    (reduce
      (fn [acc v] (update-in acc [(count v)] (partial cons v)))
      {}
      (partition-by :value cards))))

(defn- n-of-a-kind?
  ([n cards]
    (contains? (group-by-value cards) n)))

(def- four-of-a-kind?  (partial n-of-a-kind? 4))
(def- three-of-a-kind? (partial n-of-a-kind? 3))
(def- pair?            (partial n-of-a-kind? 2))

(defn- two-pair?
  ([cards]
    (= 2 (count (get (group-by-value cards) 2)))))

(defn- full-house?
  ([cards]
    (and (three-of-a-kind? cards) (pair? cards))))

;; Functions to analyse a hand and create a vector representing the rank for sorting

(defn- category
  ([cards]
    (cond
      (royal-flush?     cards) :royal-flush
      (straight-flush?  cards) :straight-flush
      (four-of-a-kind?  cards) :four-of-a-kind
      (full-house?      cards) :full-house
      (flush?           cards) :flush
      (straight?        cards) :straight
      (three-of-a-kind? cards) :three-of-a-kind
      (two-pair?        cards) :two-pair
      (pair?            cards) :pair
      :else                    :high-card)))

(defn- values-of-group
  ([n cards]
    (map (comp :value first) (get (group-by-value cards) n))))

(def- reverse-sort    (comp reverse sort))
(def- reverse-sort-by (comp reverse sort-by))

(defn- category-rank
  ([category]
    (let [ categories [ :high-card :pair :two-pair :three-of-a-kind :straight :flush
                        :full-house :four-of-a-kind :straight-flush :royal-flush ]
           ranks      (zipmap categories (range (count categories))) ]
      (get ranks category))))

;; TODO refactor into multimethod
(defn- describe-hand
  ([c kicker-rank]
    (let [[k1 k2 k3 k4 k5] (map deck/value-strs kicker-rank)]
      (cond
        (= c :royal-flush)     (format "Royal flush")
        (= c :straight-flush)  (format "Straight flush, %s high" k1)
        (= c :four-of-a-kind)  (format "Four of a kind, %s's, %s kicker" k1 k2)
        (= c :full-house)      (format "Full house, %s's over %s's" k1 k2)
        (= c :flush)           (format "Flush, %s %s %s %s %s" k1 k2 k3 k4 k5)
        (= c :straight)        (format "Straight, %s high" k1)
        (= c :three-of-a-kind) (format "Three of a kind, %s's, %s %s kickers" k1 k2 k3)
        (= c :two-pair)        (format "Two pair, %s's and %s's, %s kicker" k1 k2 k3)
        (= c :pair)            (format "Pair of %s's, %s %s %s kickers" k1 k2 k3 k4)
        (= c :high-card)       (format "High card, %s, %s %s %s %s kickers" k1 k2 k3 k4 k5)
        :else                  (throw (RuntimeException. (format "Unknown category '%s'" c)))))))

(defn- analysis-result
  ([cards category kicker-rank]
    { :cards       cards 
      :category    category
      :rank        (apply vector (cons (category-rank category) kicker-rank))
      :description (describe-hand category kicker-rank) }))

(defmulti analyse-hand category)

(defmethod analyse-hand :royal-flush
  ([cards]
    (analysis-result cards :royal-flush
      nil)))

(defmethod analyse-hand :straight-flush
  ([cards]
    (analysis-result cards :straight-flush
      [(:value (last cards))])))

(defmethod analyse-hand :four-of-a-kind
  ([cards]
    (analysis-result cards :four-of-a-kind
      (cons
        (first (values-of-group 4 cards))
        (values-of-group 1 cards)))))

(defmethod analyse-hand :full-house
  ([cards]
    (analysis-result cards :full-house
      [ (first (values-of-group 3 cards))
        (first (values-of-group 2 cards)) ])))

(defmethod analyse-hand :flush
  ([cards]
    (analysis-result cards :flush
      (reverse-sort (values-of-group 1 cards)))))

(defmethod analyse-hand :straight
  ([cards]
    (analysis-result cards :straight
      [(:value (last cards))] )))

(defmethod analyse-hand :three-of-a-kind
  ([cards]
    (analysis-result cards :three-of-a-kind
      (cons
        (first (values-of-group 3 cards))
        (reverse-sort (values-of-group 1 cards))))))

(defmethod analyse-hand :two-pair
  ([cards]
    (analysis-result cards :two-pair
      (concat
        (reverse-sort (values-of-group 2 cards))
        (reverse-sort (values-of-group 1 cards))) )))

(defmethod analyse-hand :pair
  ([cards]
    (analysis-result cards :pair
      (concat
        (values-of-group 2 cards)
        (reverse-sort (values-of-group 1 cards)) ))))

(defmethod analyse-hand :high-card
  ([cards]
    (analysis-result cards :high-card
      (reverse-sort (map :value cards)))))

;; Comparator for rank vectors which ignores vector length

(defn- compare-rank
  ([[a & a-rest] [b & b-rest]]
    (let [ result (compare b a) ]
      (cond
        (not (zero? result))                  result
        (and (empty? a-rest) (empty? b-rest)) 0
        :else                                 (recur a-rest b-rest)))))

(defn rank-hands
  "Returns the seq of hands of cards in rank order, strongest first."
  ([hands]
    (let [ sort-cards (fn [x] (sort-by :value x))]
      (->> hands
        (map sort-cards)
        (map analyse-hand)
        (sort-by :rank compare-rank)))))

;; Very inefficient but doesn't matter as we only care about all the ways to select three cards from five

(defn- select-from-set-without-replacement
  ([n x]
    (cond
      (= n (count x)) [x]
      (= 0 n)         [[]]
      :else           (concat
                        (map (partial cons (first x)) (select-from-set-without-replacement (dec n) (rest x)))
                        (select-from-set-without-replacement n (rest x))))))

(defn calculate-best-hand-per-player
  "Adds a :best-hand entry to each player describing the player's best hand from the hole cards and community cards."
  ([{:keys [players flop turn river] :as game}]
    (let [ community-cards (concat flop turn river)
           permutations    (select-from-set-without-replacement 3 community-cards)
           all-hands       (fn [p-cards] (map (partial concat p-cards) permutations))
           best-hand       (fn [p-cards] (first (rank-hands (all-hands p-cards)))) ]
      (reduce
        (fn [game id] (assoc-in game [:players id :best-hand] (best-hand (get-in game [:players id :cards]))))
        game
        (keys (game :players))))))

(defn calculate-player-rank
  ([{:keys [players] :as game}]
    (let [ hand-to-player (into {} (for [ p (vals players) ] [ (-> p :best-hand :cards) (p :id) ]))
           hand-rank      (rank-hands (keys hand-to-player))
           player-rank    (map (fn [x] (get hand-to-player (x :cards))) hand-rank) ]
      (assoc-in game [:player-rank] player-rank))))

;; TODO rename game to round
