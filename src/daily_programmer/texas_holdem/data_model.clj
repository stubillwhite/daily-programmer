(ns daily-programmer.texas-holdem.data-model)

(defn hand-player
  ([id]
    { :id        id
      :cards     []
      :best-hand nil}))

(defn hand
  ([players deck]
    { :players     (into {} (for [p players] [(p :id) (hand-player (p :id))]))
      :deck        (shuffle deck)
      :discards    []
      :flop        []
      :turn        []
      :river       []
      :player-rank nil}))

(defn game-player
  ([id]
    { :id id }))

(defn game
  ([players deck]
    { :players players
      :deck    deck
      :hand    nil }))

