(ns ecommerce.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

(def conn (db/abre-conexao!))

(db/cria-schema! conn)

(def computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M))
(def celular (model/novo-produto "Celular Caro", "/celular", 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))

(d/transact conn [computador, celular, calculadora, celular-barato])

(pprint (db/todos-os-produtos (d/db conn)))

; não é uma nova entidade
; no momento em que uso um identificador igual a algo que já existe
; é uma atualização da existente
(def celular-barato-2 (model/novo-produto (:produto/id celular-barato) "Celular baratissimo" "/celular-baratissimo" 0.001M))

(pprint @(d/transact conn [celular-barato-2]))

(pprint (db/todos-os-produtos (d/db conn)))

; o que aconteceu aqui:
; tentamos adicionar um celular-barato-2 com o mesmo id do celular-barato
; ele aceitou e sobreescreveu todos os valores do celular barato original
; isso acontece pq informamos no schema que o :produto/id é único!

;(db/apaga-banco)

; O que acontece ao tentarmos fazer um d/transact com um mapa que possui um atributo único já existente em uma entidade no banco?
; Ele sobrescreve os atributos daquela entidade e adiciona os atributos que não possuiam valor ou que eram de cardinalidade múltipla.

;Vimos nessa aula:
;
;o papel do lookup ref durante um db:add
;o cuidado de confiar no db:add como função de inclusão e alteração