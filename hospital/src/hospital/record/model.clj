(ns hospital.record.model)

(defprotocol Dateable
  (to-ms [this]))

(extend-type java.lang.Number
  Dateable
  (to-ms [this] this))

; equal to:
;(extend java.lang.Number
;  Dateable
;  {:to-ms identity})

(extend-type java.util.Date
  Dateable
  (to-ms [this] (.getTime this)))

(extend-type java.util.Calendar
  Dateable
  (to-ms [this] (to-ms (.getTime this))))
