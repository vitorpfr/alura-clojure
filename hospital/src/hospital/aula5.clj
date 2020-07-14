(ns hospital.aula5
  (:use [clojure pprint])
  (:require [hospital.model :as h.model]
            [hospital.logic :as h.logic]))

; objetivo da aula: fazer a logica do hospital usando atom e swap

; delegates
(defn chega-em! [hospital pessoa]
  (swap! hospital h.logic/chega-em :espera pessoa))


(defn transfere! [hospital de para]
  (swap! hospital h.logic/transfere de para))

;(transfere hospital :espera :laboratorio1)

(defn simula-um-dia
  []
  (let [hospital (atom (h.model/novo-hospital))]
    (chega-em! hospital "joao")
    (chega-em! hospital "maria")
    (chega-em! hospital "daniela")
    (chega-em! hospital "guilherme")
    (transfere! hospital :espera :laboratorio1)
    (transfere! hospital :espera :laboratorio2)
    (transfere! hospital :espera :laboratorio2)
    (transfere! hospital :laboratorio2 :laboratorio3)
    (pprint hospital))
  )

(simula-um-dia)

; Problema do código: passam-se algumas linhas entre olhar a proxima pessoa
; da fila e tirar de fato; pode acontecer outra coisa nesse tempo

; não é um problema porque, como a funcao é pura, a funcao nao muda de uma linha pra outra!

; caso um side-effect externo mude o atom, vai haver um retry da função, então não precisamos nos preocupar


;Nessa aula, aprendemos:
;
;Fazer a transferência de uma pessoa de uma fila para outra
;Criar Delegates
;Isolar a mutabilidade da imutabilidade
;Utilizar juxt para invocar várias funções em um parâmetro
