(ns datomic3-ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic3-ecommerce.db :as db]
            [datomic3-ecommerce.model :as model]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(db/cria-dados-de-exemplo! conn)

(pprint (db/todas-as-categorias (d/db conn)))
(pprint (db/todos-os-produtos (d/db conn)))

(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro-produto (first produtos))
(pprint primeiro-produto)

; se não encontra, devolve nil, independentemente de schema ativo ou não
(db/um-produto (d/db conn) (:produto/id primeiro-produto))
(db/um-produto (d/db conn) (model/uuid))

; se não encontra, joga um erro, independentemente de schema ativo ou não
(db/um-produto! (d/db conn) (:produto/id primeiro-produto))
(db/um-produto! (d/db conn) (model/uuid))

; o ideal é ter um catch de exceptions numa camada superior da aplicação, e condicionar o retorno ao usuario dependendo do tipo da ex (ex 404 se for not found)