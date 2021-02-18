(ns datomic6-ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic6-ecommerce.db.config :as db.config]
            [datomic6-ecommerce.db.produto :as db.produto]
            [datomic6-ecommerce.db.venda :as db.venda]
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