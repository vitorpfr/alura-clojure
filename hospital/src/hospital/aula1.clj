(ns hospital.aula1
  (:use [clojure pprint])
  (:require [hospital.model :as h.model]
            [hospital.logic :as h.logic]))

(defn simula-um-dia []
  ; root binding
  (def hospital (h.model/novo-hospital))

  ; pessoa chega no hospital e vai pra fila de espera
  (def hospital (h.logic/chega-em hospital :espera "111"))
  (def hospital (h.logic/chega-em hospital :espera "222"))
  (def hospital (h.logic/chega-em hospital :espera "333"))
  ; funciona mas não é uma boa prática variavel global e
  ; redefinição constante do simbolo que é imutavel


  (def hospital (h.logic/chega-em hospital :laboratorio1 "444"))
  (def hospital (h.logic/chega-em hospital :laboratorio3 "555"))
  (println "hospital após tood mundo chegar")
  (pprint hospital)

  ; atende: deve atender o primeiro da fila de espera
  (def hospital (h.logic/atende hospital :laboratorio1))
  (def hospital (h.logic/atende hospital :espera))

  (println "hospital após o atendimento de duas pessoas")
  (pprint hospital)


  ;(simula-um-dia)


  ;Nessa aula, aprendemos:
  ;
  ;Criar filas vazias
  ;Adicionar elementos em uma fila utilizando conj
  ;Remover o primeiro elemento da fila utilizando pop
  ;Selecionar o primeiro elemento da fila utilizando peek
  ;Atualizar o valor do mapa utilizando update


  ; aula 2

  (println "chegaram mais pessoas")
  (def hospital (h.logic/chega-em hospital :espera "666"))
  (def hospital (h.logic/chega-em hospital :espera "777"))
  (def hospital (h.logic/chega-em hospital :espera "888"))
  (pprint hospital)

  ; estoura o tamanho da fila - exception
  (def hospital (h.logic/chega-em hospital :espera "999"))
  (pprint hospital)
  )



(defn chega-na-espera-redef
  [pessoa]
  (def hospital (h.logic/chega-em-pausado hospital :espera pessoa))
  (println "apos inserir" pessoa))


; simulando threads em paralelo: não são executadas na ordem
; fica claro o problema de variavel global - concorrencia
(println "simulando dia com threads em paralelo")
(defn simula-um-dia-em-paralelo
  []
  (def hospital (h.model/novo-hospital))
  (.start (Thread. (fn [] (chega-na-espera-redef "111"))))
  (.start (Thread. (fn [] (chega-na-espera-redef "222"))))
  (.start (Thread. (fn [] (chega-na-espera-redef "333"))))
  (.start (Thread. (fn [] (chega-na-espera-redef "444"))))
  (.start (Thread. (fn [] (chega-na-espera-redef "555"))))
  (.start (Thread. (fn [] (chega-na-espera-redef "666"))))
  (.start (Thread. (fn [] ((Thread/sleep 4000)))))
  (pprint hospital)
  )



(simula-um-dia-em-paralelo)

;Nessa aula, aprendemos:
;
;Implementar um limite à fila fazendo uso do if
;Verificar o tamanho da fila utilizando count
;Tratar erros não previstos com a função ex-info
;Trabalhar com a compatibilidade Java ao criar a classe Thread
;Iniciar uma Thread com o método start
;Compreender os problemas que existem ao utilizarmos símbolos globais compartilhados entre threads
