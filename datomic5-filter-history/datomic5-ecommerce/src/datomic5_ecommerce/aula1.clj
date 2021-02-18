(ns datomic5-ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic5-ecommerce.db.config :as db.config]
            [datomic5-ecommerce.db.produto :as db.produto]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

; Passo a conexão ou o db direto?
; Depende de quem eu quero que vá decidir o momento no tempo que quero acessar o db (essa camada ou a camada do db)
(def produtos (db.produto/todos (d/db conn)))
(def primeiro (first produtos))
(def ultimo (last produtos))

ultimo