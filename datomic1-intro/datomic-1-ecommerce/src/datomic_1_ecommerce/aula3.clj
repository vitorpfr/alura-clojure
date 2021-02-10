(ns datomic-1-ecommerce.aula3
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

(db/todos-os-produtos-por-slug-fixo (d/db conn))

(db/todos-os-produtos-por-slug (d/db conn) "/computador-novo")

(db/todos-os-slugs (d/db conn))

(db/todos-os-produtos-por-preco (d/db conn))