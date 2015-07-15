(ns daily-programmer.simple-stream-cypher.cypher
  (:require
    [clojure.math.numeric-tower :as numeric]
    [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn- next-number
  ([x]
    (let [ a 1103515245
           m (numeric/expt 2 31)
           c 12345 ]
      (mod (+ (* a x) c) m))))

(defn- linear-congruential-generator
  ([seed]
    (drop 1 (iterate next-number seed))))

(def rng linear-congruential-generator)

(defn- to-byte-stream
  ([msg]
    (map int (seq msg))))

(defn- xor-byte-streams
  ([a b]
    (map
      (fn [[x y]] (bit-xor x y))
      (partition 2 (interleave a b)))))

(defn encrypt
  ([key msg]
    (xor-byte-streams (rng key) (to-byte-stream msg))))

(defn decrypt
  ([key msg]
    (apply str (map char (xor-byte-streams (rng key) msg)))))

(defn test-roundtripping-message
  ([]
    (let [ msg       "This is a test message."
           key       42
           encrypted (encrypt key msg)
           decrypted (decrypt key encrypted) ]
      (println "Message:   " msg)
      (println "Encrypted: " encrypted)
      (println "Decrypted: " decrypted))))
