(ns daily-programmer.texas-holdem.ai)

(defn always-check
  "A dumb AI that always checks."
  ([id hand action-phase]
    :check))

(defn fold-if-no-face-cards
  "A dumb AI that folds if its hole cards do not contain a face card."
  ([id hand action-phase]
    (if (= action-phase :before-the-flop)
      (let [ hole-cards (get-in hand [:hands-of-cards id :hole-cards])
             face-card? (fn [x] (> (x :value) 10)) ]
        (if (seq (filter face-card? hole-cards))
          :check
          :fold))
      :check)))

