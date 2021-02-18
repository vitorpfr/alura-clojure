(ns datomic5-ecommerce.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic5-ecommerce.db.config :as db.config]
            [datomic5-ecommerce.db.produto :as db.produto]
            [datomic5-ecommerce.db.venda :as db.venda]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(def produtos (db.produto/todos (d/db conn)))
(def primeiro (first produtos))
(def ultimo (last produtos))

ultimo

(def venda1 (db.venda/adiciona! conn (:produto/id ultimo) 3))
(def venda2 (db.venda/adiciona! conn (:produto/id ultimo) 4))
(def venda3 (db.venda/adiciona! conn (:produto/id ultimo) 8))

(pprint venda1)

(pprint @(db.venda/altera-situacao! conn venda1 "preparando"))
(pprint @(db.venda/altera-situacao! conn venda2 "preparando"))
(pprint @(db.venda/altera-situacao! conn venda2 "a caminho"))
(pprint @(db.venda/altera-situacao! conn venda2 "entregue"))

(pprint (db.venda/historico (d/db conn) venda2))

(db.venda/cancela! conn venda1)

(pprint (db.venda/historico (d/db conn) venda1))

; as queries abaixo foram refatoradas pra usar a situação ao invés do historico
(pprint (count (db.venda/ativas (d/db conn))))              ; 2
(pprint (count (db.venda/todas (d/db conn))))               ; 3
(pprint (count (db.venda/canceladas (d/db conn))))          ; 1

; agora quero o historico das vendas a partir de um determinado momento
(pprint (db.venda/historico-geral (d/db conn) #inst "2011-01-18T19:24:49.421-00:00"))
; desse jeito ele traz o ultimo datom valido de cada venda

; aqui traz tudo vazio (usando um tempo intermediario)
(pprint (db.venda/historico-geral (d/db conn) #inst "2021-02-18T19:24:49.407-00:00"))
; o problema é que, quando eu uso o "since" entre a nova venda e a atualização, ele não traz o datom da criação do id da venda!
; e a query procura um venda/id

; resumo: as vezes voce vai usar o since puro
; mas se na mesma query vc quer usar dados de um datom no passado, o since puro não funciona