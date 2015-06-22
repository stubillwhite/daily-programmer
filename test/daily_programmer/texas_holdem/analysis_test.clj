(ns daily-programmer.texas-holdem.analysis-test
  (:require
    [daily-programmer.texas-holdem.deck :as deck]
    [clojure.string :as string])
  (:use
    [expectations]
    [daily-programmer.texas-holdem.analysis]))

(defn- create-cards
  ([cards-str]
    (let [ suit-symbols  (zipmap [\C \D \H \S] deck/suits)
           value-symbols (zipmap [\2 \3 \4 \5 \6 \7 \8 \9 \X \J \Q \K \A] deck/values)
           create-card   (fn [[v s]]  { :value (get value-symbols v)
                                       :suit  (get suit-symbols s) }) ]
      (map create-card (string/split cards-str #" ")))))

;; Basic hand analysis

(defn- category-and-rank
  ([result]
    (select-keys result [:category :rank])))

(expect {:category :royal-flush      :rank [9]}            (category-and-rank (analyse-hand (create-cards "XC JC QC KC AC"))))
(expect {:category :straight-flush   :rank [8 6]}          (category-and-rank (analyse-hand (create-cards "2C 3C 4C 5C 6C"))))
(expect {:category :four-of-a-kind   :rank [7 3 2]}        (category-and-rank (analyse-hand (create-cards "3C 3D 3H 3S 2C"))))
(expect {:category :full-house       :rank [6 3 2]}        (category-and-rank (analyse-hand (create-cards "2C 2D 3H 3S 3C"))))
(expect {:category :flush            :rank [5 10 8 6 4 2]} (category-and-rank (analyse-hand (create-cards "2C 4C 6C 8C XC"))))
(expect {:category :straight         :rank [4 6]}          (category-and-rank (analyse-hand (create-cards "2C 3D 4H 5S 6C"))))
(expect {:category :three-of-a-kind  :rank [3 2 9 8]}      (category-and-rank (analyse-hand (create-cards "2C 2D 2H 8S 9C"))))
(expect {:category :two-pair         :rank [2 3 2 10]}     (category-and-rank (analyse-hand (create-cards "2C 2D 3H 3S XC"))))
(expect {:category :pair             :rank [1 3 10 8 2]}   (category-and-rank (analyse-hand (create-cards "3C 3D 2H 8S XC"))))
(expect {:category :high-card        :rank [0 10 8 6 4 2]} (category-and-rank (analyse-hand (create-cards "2C 4D 6H 8S XC"))))





