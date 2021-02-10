(ns datomic-1-ecommerce.aula5
  (:use clojure.pprint)
  (:require [datomic-1-ecommerce.db :as db]
            [datomic-1-ecommerce.model :as m]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

(let [computador (m/novo-produto "Computador Novo" "/computador-novo" 2500.10M)
      celular-caro (m/novo-produto "Celular Caro" "/celular" 8888.10M)
      resultado @(db/transaciona conn [computador celular-caro])]
  resultado
  )

; meu snapshot
; (poderia usar o :db-after da ultima transação tb)
(def fotografia-do-passado (d/db conn))

(let [calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (m/novo-produto "Celular Barato" "/celular-barato" 0.1M)
      resultado @(db/transaciona conn [calculadora celular-barato])]
  resultado
  )

; rodando um snapshot no instante do d/db - 4 elementos
(count (db/todos-os-produtos (d/db conn)))

; rodando a query num banco filtrado com dados do passado - 2 elementos
(count (db/todos-os-produtos fotografia-do-passado))

; rodando a query num banco no inst antes d 2 transacoes - 0 elementos
(count (db/todos-os-produtos (d/as-of (d/db conn) #inst"2021-02-08T22:37:55.748-00:00")))

; rodando a query num banco no inst entre as 2 transacoes - 2 elementos
(count (db/todos-os-produtos (d/as-of (d/db conn) #inst"2021-02-08T22:37:58.748-00:00")))

; rodando a query num banco no inst depois das 2 transacoes - 4 elementos
(count (db/todos-os-produtos (d/as-of (d/db conn) #inst"2021-02-08T22:38:58.748-00:00")))

