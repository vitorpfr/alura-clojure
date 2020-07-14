(ns hospital.model
  (:require [schema.core :as s]))

(def fila-vazia clojure.lang.PersistentQueue/EMPTY)

(defn novo-hospital []
  {:espera       fila-vazia
   :laboratorio1 fila-vazia
   :laboratorio2 fila-vazia
   :laboratorio3 fila-vazia})

(defn novo-departamento []
  fila-vazia)

(s/def Paciente s/Str)
(s/def Departamento (s/queue Paciente))
(s/def Hospital {s/Keyword Departamento})

; ilustrativo para a aula
;(s/validate Paciente "vitor")
;(s/validate Paciente 15)
