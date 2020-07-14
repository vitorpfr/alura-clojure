(ns ecommerce.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

; apaga banco - opcional pra resetar tudo
(db/apaga-banco)

; abre conexão com banco
(def conn (db/abre-conexao))

; cria schema dentro do banco
@(db/cria-schema conn)

; insere um valor no banco só com um dos valores (nome)
; o datomic suporta somente um dos identificadores na hora de inserir um produto
(let [calculadora {:produto/nome "Calculadora com 4 operações"}]
  @(d/transact conn [calculadora]))

; não funciona pois se você quer algo "vazio", é só não colocar
;(let [radio-relogio {:produto/nome "Rádio com relógio" :produto/slug nil}]
;  @(d/transact conn [radio-relogio]))

; insere um valor no banco
(let [celular-barato (model/novo-produto "Celular Barato" "/celular-barato" 8888.10M) ; schema do produto a ser adicionado
      resultado @(d/transact conn [celular-barato])         ; resultado da adicao do produto
      id-entidade (first (vals (:tempids resultado)))]    ; pegar o id do produto adicionado
  (pprint resultado)
  (pprint @(d/transact conn [[:db/add id-entidade :produto/preco 0.1M]])) ; alterar valor de produto ja adicionado
  ; resultado: #datom[17592186045437 74 0.1M 13194139534334 true] #datom[17592186045437 74 8888.10M 13194139534334 false]],
  (pprint @(d/transact conn [[:db/retract id-entidade :produto/slug "/celular-barato"]]))
  )

; apaga banco - opcional pra resetar tudo
(db/apaga-banco)

; Aula 2
;Nessa aula vimos:
;
;db:retract
;db:add funcionando também como atualização de um valor
;o uso do db:id como identificador único de uma entidade
