(ns hospital.aula6
  (:use [clojure pprint])
  (:require [hospital.model :as h.model]))

; as vezes é melhor criar varios atoms ao inves de um grande atom,
; pois um laboratorio nao interfere no outro
; entretanto, atom não suporta essas transações
; usaremos ref nesse caso que suportam mutabilidade e
; transações entre refs diferentes

(defn cabe-na-fila?
  [fila]
  (-> fila
      count
      (< ,,, 5)))

(defn chega-em [fila pessoa]
  (if (cabe-na-fila? fila)
    (conj fila pessoa)
    (throw (ex-info "Fila já está cheia" {:tentando-adicionar pessoa})))
  )

(defn chega-em! [hospital pessoa]
  "troca de referência via ref-set"
  (let [fila (get hospital :espera)]
    (ref-set fila (chega-em @fila pessoa))))

; @fila poderia ser (deref fila)
; motivo: o chega-em é uma função pura, por isso ele não recebe o ref, e sim
; o que está dentro do ref (a fila em si), por isso precisa dereferenciar;
;outra opcao é usar o alter, como abaixo

(defn chega-em! [hospital pessoa]
  "troca de referência via alter"
  (let [fila (get hospital :espera)]
    (alter fila chega-em pessoa)))

(defn simula-um-dia []
  (let [hospital {:espera       (ref h.model/fila-vazia)
                  :laboratorio1 (ref h.model/fila-vazia)
                  :laboratorio2 (ref h.model/fila-vazia)
                  :laboratorio3 (ref h.model/fila-vazia)}]
    (dosync           ; precisa do dosync prq o ref-set precisa de uma transação rodando
      (chega-em! hospital "guilherme")
      (chega-em! hospital "maria")
      (chega-em! hospital "lucia")
      (chega-em! hospital "daniela")
      (chega-em! hospital "ana")
      ;(chega-em! hospital "paulo")
      )
    (pprint hospital))
    )

;(simula-um-dia)

(defn async-chega-em! [hospital pessoa]
  (future
    (Thread/sleep (rand 5000))
    (dosync
      (println "Tentando o codigo sincronizado" pessoa)
      (chega-em! hospital pessoa))))


(defn simula-um-dia-async []
  (let [hospital {:espera       (ref h.model/fila-vazia)
                  :laboratorio1 (ref h.model/fila-vazia)
                  :laboratorio2 (ref h.model/fila-vazia)
                  :laboratorio3 (ref h.model/fila-vazia)}
        futures (mapv #(async-chega-em! hospital %) (range 10))]

    (dotimes [pessoa 10] (async-chega-em! hospital pessoa))
    (future
      (dotimes [n 4]
        (Thread/sleep 2000)
        (pprint hospital)
        (pprint futures))))
  )

(simula-um-dia-async)


;Nessa aula, aprendemos:
;
;Utilizar ref para criar mutabilidade isolada com controle transacional
;Trocar o valor de uma referência através de ref-set e alter
;Trabalhar com transações utilizando dosync
;Executar um código e utilizar o resultado apenas no “futuro” com future
