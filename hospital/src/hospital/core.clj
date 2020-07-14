(ns hospital.core
  (:use [clojure pprint])
  (:require [hospital.model :as h.model])
  )


; espera (FILA DE ESPERA)

; pessoas chegam e entram naz fila de espera.
; a medida que os labs tem espaço, as pessaos saem da espera
; e vão pra fila de um dos labs.

; laboratorio 1 (FILA 1)
; laboratorio 2 (FILA 2)
; laboratorio 3 (FILA 3)

; quando as pessoas vão sendo atendidas, elas saem das filas
; dos laboratorios

(let [meu-hospital (h.model/novo-hospital) ]
  (pprint meu-hospital))


(pprint h.model/fila-vazia)

