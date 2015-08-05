(ns daily-programmer.texas-holdem.analysis
  (:require
    [clojure.string :as string]
    [daily-programmer.texas-holdem.deck :as deck]
    [taoensso.timbre :as timbre])
  (:use
    [daily-programmer.utils :refer [def- defmulti- defmethod-]]))

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

(defn- categorise-hand
  ([hand]
    (cond
      (royal-flush?     hand) :royal-flush
      (straight-flush?  hand) :straight-flush
      (four-of-a-kind?  hand) :four-of-a-kind
      (full-house?      hand) :full-house
      (flush?           hand) :flush
      (straight?        hand) :straight
      (three-of-a-kind? hand) :three-of-a-kind
      (two-pair?        hand) :two-pair
      (pair?            hand) :pair
      :else                   :high-card)))

(defn- values-of-group
  ([n cards]
    (map (comp :value first) (get (group-by-value cards) n))))

(def- reverse-sort (comp reverse sort))

(defn- category-rank
  ([category]
    (let [ categories [ :high-card :pair :two-pair :three-of-a-kind :straight :flush
                        :full-house :four-of-a-kind :straight-flush :royal-flush ]
           ranks      (zipmap categories (range (count categories))) ]
      (get ranks category))))

(defn- create-description
  ([fmt rank]
    (let [[category k1 k2 k3 k4 k5] (map deck/value-strs rank)]
      (format fmt k1 k2 k3 k4 k5))))

(defmulti- describe-hand (fn [category rank] category))
(defmethod- describe-hand :royal-flush     [category rank] (create-description "Royal flush" rank))
(defmethod- describe-hand :straight-flush  [category rank] (create-description "Straight flush, %s high" rank))
(defmethod- describe-hand :four-of-a-kind  [category rank] (create-description "Four of a kind, %s's, %s kicker" rank))
(defmethod- describe-hand :full-house      [category rank] (create-description "Full house, %s's over %s's" rank))
(defmethod- describe-hand :flush           [category rank] (create-description "Flush, %s %s %s %s %s" rank))
(defmethod- describe-hand :straight        [category rank] (create-description "Straight, %s high" rank))
(defmethod- describe-hand :three-of-a-kind [category rank] (create-description "Three of a kind, %s's, %s %s kickers" rank))
(defmethod- describe-hand :two-pair        [category rank] (create-description "Two pair, %s's and %s's, %s kicker" rank))
(defmethod- describe-hand :pair            [category rank] (create-description "Pair of %s's, %s %s %s kickers" rank))
(defmethod- describe-hand :high-card       [category rank] (create-description "High card, %s, %s %s %s %s kickers" rank))

(defn- create-rank
  ([category kickers]
    (apply vector (cons (category-rank category) kickers))))

(defmulti- rank-hand (fn [category cards] category))
(defmethod- rank-hand :royal-flush     [category cards] (create-rank category nil))
(defmethod- rank-hand :straight-flush  [category cards] (create-rank category [(:value (last cards))]))
(defmethod- rank-hand :four-of-a-kind  [category cards] (create-rank category (cons (first (values-of-group 4 cards)) (values-of-group 1 cards))))
(defmethod- rank-hand :full-house      [category cards] (create-rank category [ (first (values-of-group 3 cards)) (first (values-of-group 2 cards)) ]))
(defmethod- rank-hand :flush           [category cards] (create-rank category (reverse-sort (values-of-group 1 cards))))
(defmethod- rank-hand :straight        [category cards] (create-rank category [(:value (last cards))]))
(defmethod- rank-hand :three-of-a-kind [category cards] (create-rank category (cons (first (values-of-group 3 cards)) (reverse-sort (values-of-group 1 cards)))))
(defmethod- rank-hand :two-pair        [category cards] (create-rank category (concat (reverse-sort (values-of-group 2 cards)) (values-of-group 1 cards))))
(defmethod- rank-hand :pair            [category cards] (create-rank category (concat (values-of-group 2 cards) (reverse-sort (values-of-group 1 cards)))))
(defmethod- rank-hand :high-card       [category cards] (create-rank category (reverse-sort (map :value cards))))

(defn analyse-hand
  ([hand]
    (let [ category (categorise-hand hand)
           rank     (rank-hand category hand) ]
      { :hand        hand
        :category    category
        :rank        rank
        :description (describe-hand category rank) })))

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
  ([{:keys [flop turn river] :as hand}]
    (let [ community-cards (concat flop turn river)
           permutations    (select-from-set-without-replacement 3 community-cards)
           all-hands       (fn [p-cards] (map (partial concat p-cards) permutations))
           best-hand       (fn [p-cards] (first (rank-hands (all-hands p-cards)))) ]
      (reduce
        (fn [hand id] (assoc-in hand [:hands-of-cards id :best-hand] (best-hand (get-in hand [:hands-of-cards id :hole-cards]))))
        hand
        (keys (hand :hands-of-cards))))))

(defn calculate-player-rank
  "Adds a :player-rank entry t the hand describing the rank order of each player's hand."
  ([{:keys [hands-of-cards] :as hand}]
    (let [ hand-to-player (into {} (for [id (keys hands-of-cards)] [(get-in hands-of-cards [id :best-hand :hand]) id]))
           hand-rank      (rank-hands (keys hand-to-player))
           player-rank    (map (fn [x] (get hand-to-player (x :hand))) hand-rank) ]
      (assoc-in hand [:player-rank] player-rank))))
