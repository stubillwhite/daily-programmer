(ns daily-programmer.texas-holdem.data-model)

(defn hand-of-cards
  ([]
    { :hole-cards []
      :best-hand  nil }))

(defn hand-of-game
  ([players deck]
    { :players        (into {} (for [p players] [(p :id) p]))
      :phase          nil
      :player-actions {}
      :hands-of-cards (into {} (for [p players] [(p :id) (hand-of-cards)]))
      :deck           (shuffle deck)
      :common-cards   { :discards []
                        :flop     []
                        :turn     []
                        :river    [] }
      :player-rank    nil}))

(defn player
  ([id action-func]
    { :id          id
      :action-func action-func }))

(defn game
  ([players deck]
    { :players      players
      :deck         deck
      :hand-of-game nil }))
