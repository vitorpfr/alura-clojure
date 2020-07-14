(ns hospital.record.logic
  (:require [hospital.record.model :as h.r.model]))

(defn agora []
  (h.r.model/to-ms (java.util.Date.)))
