(ns hospital.aula4
  (:use [clojure pprint])
  (:require [hospital.model :as h.model]
            [hospital.logic :as h.logic]))

(defn chega-na-espera-sem-malvado!
  [hospital pessoa]
  (swap! hospital h.logic/chega-em :espera pessoa)
  (println "apos inserir" pessoa))



(defn simula-um-dia-em-paralelo-com-mapv
  "Simulação usando mapv para forçar quase que imperativamente a execução do que era lazy"
  []
  (let [hospital (atom (h.model/novo-hospital))
        pessoas ["111", "222", "333", "444", "555", "666"]]

    (mapv #(.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital %)))) pessoas)


    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

;(simula-um-dia-em-paralelo-com-mapv)


(defn simula-um-dia-em-paralelo-com-mapv-refatorada
  []
  (let [hospital (atom (h.model/novo-hospital))
        pessoas ["111", "222", "333", "444", "555", "666"]
        starta-thread-de-chegada #(.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital %))))]

    (mapv starta-thread-de-chegada pessoas)


    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

;(simula-um-dia-em-paralelo-com-mapv-refatorada)



(defn starta-thread-de-chegada
  ([hospital]
   (fn [pessoa] (starta-thread-de-chegada hospital pessoa)))
  ([hospital pessoa]
   (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital pessoa)))))
  )

; função parcial: como o map precisa receber só pessoa, essa funcao prepara o dado, e o
; output dela é o input do mapv
; implementada dentro da fn acima
;(defn preparadinha
;  [hospital]
;  (fn [pessoa] (starta-thread-de-chegada hospital pessoa))
;  )

(defn simula-um-dia-em-paralelo-com-mapv-extraida
  []
  (let [hospital (atom (h.model/novo-hospital))
        pessoas ["111", "222", "333", "444", "555", "666"]
        starta (starta-thread-de-chegada hospital)]

    (mapv starta pessoas)


    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

;(simula-um-dia-em-paralelo-com-mapv-extraida)



; funcao que prepara a funcao

(defn starta-thread-de-chegada
  [hospital pessoa]
  (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital pessoa))))
  )

; função parcial: como o map precisa receber só pessoa, essa funcao prepara o dado, e o
; output dela é o input do mapv
; implementada dentro da fn acima
;(defn preparadinha
;  [hospital]
;  (fn [pessoa] (starta-thread-de-chegada hospital pessoa))
;  )

(defn simula-um-dia-em-paralelo-com-mapv-partial
  []
  (let [hospital (atom (h.model/novo-hospital))
        pessoas ["111", "222", "333", "444", "555", "666"]
        starta (partial starta-thread-de-chegada hospital)]

    (mapv starta pessoas)


    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

;(simula-um-dia-em-paralelo-com-mapv-partial)










; do seq: executa bloco de codigo n vezes para side-effects


(defn starta-thread-de-chegada
  [hospital pessoa]
  (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital pessoa))))
  )

(defn simula-um-dia-em-paralelo-com-doseq
  "Util quando quero executar para os elementos da sequencia (vetor)"
  []
  (let [hospital (atom (h.model/novo-hospital))
        pessoas ["111", "222", "333", "444", "555", "666"]]

    (doseq [pessoa pessoas]
      (starta-thread-de-chegada hospital pessoa))
    ; doseq executa o corpo repetidamente pra cada elemento de "pessoas", e faz algo com a "pessoa"


    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

;(simula-um-dia-em-paralelo-com-doseq)














(defn simula-um-dia-em-paralelo-com-dotimes
  "Util quando quero executar n vezes"
  []
  (let [hospital (atom (h.model/novo-hospital))]

    (dotimes [pessoa 6]
      (starta-thread-de-chegada hospital pessoa))
    ; doseq executa o corpo repetidamente pra cada elemento de "pessoas", e faz algo com a "pessoa"


    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )


(simula-um-dia-em-paralelo-com-dotimes)

; importante: se uma função não retorna nada, provavelmente deve ter efeito colateral (ex: doseq)
; se não retorna nada e não tem efeito colateral, podemos remover!

;Nessa aula, aprendemos:
;
;Utilizar mapv para forçar a execução de uma função
;Aumentar a legibilidade do código extraindo responsabilidades das funções
;Implementar uma chamada parcial de uma função utilizando partial
;Utilizar doseq para iterar por uma sequência de elementos
;Utilizar dotimes para executar uma tarefa um número fixo de vezes
