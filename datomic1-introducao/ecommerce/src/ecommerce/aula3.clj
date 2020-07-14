(ns ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))


; abre conexão com banco
(def conn (db/abre-conexao))

; cria schema dentro do banco
@(db/cria-schema conn)

; insere vários produtos no banco
(let [computador (model/novo-produto "Computador Novo" "/computador-novo" 2500.10M)
      impressora (model/novo-produto "Impressora Nova" "/impressora" 999.990M)
      celular (model/novo-produto "Celular Caro" "/celular" 8888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato" "/celular-barato" 0.1M)]
  @(d/transact conn [computador impressora celular calculadora celular-barato]))

(pprint (db/todos-os-produtos (d/db conn)))
(pprint (db/todos-os-produtos-por-slug-fixo (d/db conn)))
(pprint (db/todos-os-produtos-por-slug (d/db conn) "/computador-novo"))

(pprint (db/todos-os-slugs (d/db conn)))

(pprint (db/todos-os-produtos-por-preco (d/db conn)))

;(db/apaga-banco)

; Aula 3
;Nessa aula vimos:
;
;como fazer queries por entidades
;como buscar por atributo
;como extrair atributo
;como funciona o binding de variáveis em queries

; Aula 4
;Vimos nessa aula:
;
;como buscar atributos com o pull
;como buscar todos os atributos com o pull
