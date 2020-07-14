(ns hospital.aula3
  (:use [clojure pprint])
  (:require [hospital.model :as h.model]
            [hospital.logic :as h.logic]))

; simbolo que qualquer thread que acessar esse namespace vai ter acesso
; com o valor padrão "guilherme"
(def nome "guilherme")

; redefini o simbolo (refiz o binding dele)
(def nome 332)

; alternativa: let

(let [nome "guilherme"]
  ; coisa 1
  ; coisa 2
  (println nome)
  ; nao estou refazendo o binding do simbolo local
  ; estou criando um novo simbolo local a este bloco e escondendo o anterior
  ; SHADOWING
  (let [nome "joao"]
    ; coisa 3
    ; coisa 4
    ;(println nome)
    )
  ;(println nome)
  )

; solução: ter alguns momentos onde queremos alterar um valor compartilhado

; atom: é uma "casca" que guarda um valor dentro que pode ser alterado
; deref: permite acessar o valor dentro do atom
; deref ou @
(defn testa-atomao []
  (let [hospital-silveira (atom {:espera h.model/fila-vazia})]
    (println hospital-silveira)                             ; atom completo
    (pprint hospital-silveira)                              ; atom completo
    (pprint (deref hospital-silveira))                      ; valor dentro do atom
    (pprint @hospital-silveira)                             ; valor dentro do atom

    ; não é assim que altera conteudo dentro de um atom
    ; deref é só pra ver o valor
    (pprint (assoc @hospital-silveira :laboratorio1 h.model/fila-vazia))
    (pprint @hospital-silveira)                              ;laboratorio1 não foi adicionado

    ; exclamação em função indica que vai ter um efeito colateral
    ; nesse caso, é explícito que o valor de hospital-silveira vai ser alterado
    ; por causa dessa exclamação (tem um efeito colateral)

    ; essa é uma das maneiras de alterar conteudo dentro de um atom
    (swap! hospital-silveira assoc :laboratorio1 h.model/fila-vazia)
    (pprint @hospital-silveira)

    (swap! hospital-silveira assoc :laboratorio2 h.model/fila-vazia)
    (pprint @hospital-silveira)

    ; adicionar uma pessoa
    ; update tradicional imutavel com dereferencia, que não trará efeito
    (update @hospital-silveira :laboratorio1 conj "111")

    ; indo pra swap
    (swap! hospital-silveira update :laboratorio1 conj "111")
    (pprint @hospital-silveira)

    ))



;(testa-atomao)


;; testando codigo da aula passada usando o conceito novo de atom

(defn chega-na-espera!
  [hospital pessoa]
  (swap! hospital h.logic/chega-em-pausado-logando :espera pessoa)
  (println "apos inserir" pessoa))


; simulando threads em paralelo: não são executadas na ordem
; fica claro o problema de variavel global - concorrencia
(println "simulando dia com threads em paralelo")
(defn simula-um-dia-em-paralelo
  []
  (let [hospital (atom (h.model/novo-hospital))]
    (.start (Thread. (fn [] (chega-na-espera! hospital "111"))))
    (.start (Thread. (fn [] (chega-na-espera! hospital "222"))))
    (.start (Thread. (fn [] (chega-na-espera! hospital "333"))))
    (.start (Thread. (fn [] (chega-na-espera! hospital "444"))))
    (.start (Thread. (fn [] (chega-na-espera! hospital "555"))))
    (.start (Thread. (fn [] (chega-na-espera! hospital "666"))))
    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

; o swap fica retentando se detectar alterações no atom durante a execução
; ex: comecou a adicionar o 3, comecou e adicionou o 4, a thread do 3 vai perceber
; que o atom mudou e vai retentar com o atom atualizado
; elimina problema de concorrência!
;(simula-um-dia-em-paralelo)


(defn chega-na-espera-sem-malvado!
  [hospital pessoa]
  (swap! hospital h.logic/chega-em :espera pessoa)
  (println "apos inserir" pessoa))


; simulando threads em paralelo: não são executadas na ordem
; fica claro o problema de variavel global - concorrencia
(println "simulando dia com threads em paralelo")
(defn simula-um-dia-em-paralelo
  []
  (let [hospital (atom (h.model/novo-hospital))]
    (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital "111"))))
    (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital "222"))))
    (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital "333"))))
    (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital "444"))))
    (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital "555"))))
    (.start (Thread. (fn [] (chega-na-espera-sem-malvado! hospital "666"))))
    (.start (Thread. (fn []
                       (Thread/sleep 8000)
                       (pprint hospital))))
    )
  )

; sem forçar situação de retry, pode acontecer, mas pode não acontecer
(simula-um-dia-em-paralelo)

;Apesar da abordagem mais comum de Clojure ser o sistema de retry de transações com átomos, a linguagem disponibiliza uma forma de trabalhar com locking também como o uso de travas de monitoramento com https://clojuredocs.org/clojure.core/locking


;Nessa aula, aprendemos:
;
;Implementar um limite à fila fazendo uso do if
;Evitar o uso de símbolos globais Root Binding
;Transformar um mapa imutável em mutável através do uso do atom
;Dereferenciar o átomo com deref para acessar a fila de espera dentro de um mapa
;Usar Shadowing para “esconder” um símbolo local
;Alterar o conteúdo de dentro do átomo usando swap!
;Usar o swap para evitar o problema de concorrência


