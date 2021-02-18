(ns datomic5-ecommerce.aula4
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

(db.venda/cancela! conn venda1)

(pprint (count (db.venda/ativas (d/db conn))))

; e se eu quiser todas, inclusive as canceladas?
(pprint (count (db.venda/todas (d/db conn))))

; só as vendas canceladas
(pprint (count (db.venda/canceladas (d/db conn))))


; alterando preços
(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id ultimo)
                                                :produto/preco 300M}]))
(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id ultimo)
                                                :produto/preco 250M}]))
(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id ultimo)
                                                :produto/preco 277M}]))
(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id ultimo)
                                                :produto/preco 21M}]))

; agora quero ver o historico de precos
(pprint (db.produto/historico-de-precos (d/db conn) (:produto/id ultimo)))