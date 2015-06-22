(ns daily-programmer.utils)

(defmacro def-
  ([item value]
    `(def ^{:private true} ~item ~value)))
