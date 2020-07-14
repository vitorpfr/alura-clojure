(ns ecommerce.aula5
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
      celular (model/novo-produto "Celular Caro" "/celular" 8888.10M)
      resultado @(d/transact conn [computador celular])]
  (pprint resultado))

; meu snapshot
(def fotografia-no-passado (d/db conn))

; insere vários produtos no banco
(let [calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato" "/celular-barato" 0.1M)
      resultado @(d/transact conn [calculadora celular-barato])]
  (pprint resultado))

; um snapshot no instante que o d/db é executado = 4
(pprint (count (db/todos-os-produtos (d/db conn))))

; um snapshot num instante específico (banco filtrado) com dados do passado = 2
(pprint (count (db/todos-os-produtos fotografia-no-passado)))

; antes = 0
(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2020-04-11T19:27:22.130-00:00"))))

; no meio = 2
(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2020-04-11T19:27:23.130-00:00"))))

; depois = 4
(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2020-04-11T19:27:24.130-00:00"))))

(db/apaga-banco)


; Aula 5
;Vimos nessa aula:
;
;como o Datomic armazena os dados históricos
;como acessar o banco em um momento do tempo com o as-of
