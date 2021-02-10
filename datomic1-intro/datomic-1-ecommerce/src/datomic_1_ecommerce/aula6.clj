(ns datomic-1-ecommerce.aula6
  (:use clojure.pprint)
  (:require [datomic-1-ecommerce.db :as db]
            [datomic-1-ecommerce.model :as m]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

; adicionar produtos
(let [computador (m/novo-produto "Computador Novo" "/computador-novo" 2500.10M)
      celular-caro (m/novo-produto "Celular Caro" "/celular" 8888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (m/novo-produto "Celular Barato" "/celular-barato" 0.1M)]
  (db/transaciona conn [computador celular-caro calculadora celular-barato]))

; fazer query pra pegar todos os produtos
(db/todos-os-produtos (d/db conn))

; tem que trazer 2
(db/todos-os-produtos-por-preco-minimo (d/db conn) 1000)

; tem que trazer 1
(db/todos-os-produtos-por-preco-minimo (d/db conn) 5000)

