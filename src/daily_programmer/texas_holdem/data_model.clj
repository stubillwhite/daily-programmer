(ns daily-programmer.texas-holdem.data-model)

(defn hand-of-cards
  ([]
    { :hole-cards []
      :best-hand  nil }))

(defn hand-of-game
  ([players deck]
    { :players        (into  {} (for [p players] [(p :id) p]))
      :participants   (into #{} (map :id players))
      :hands-of-cards (into  {} (for [p players] [(p :id) (hand-of-cards)]))
      :player-actions {}
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
    { :players players
      :deck    deck }))
