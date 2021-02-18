(ns datomic3-ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic3-ecommerce.db :as db]
            [datomic3-ecommerce.model :as model]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(defn testa-schema []
  (def computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M))
  (s/validate model/Produto computador)
  (def eletronicos (model/nova-categoria "Eletrônicos"))
  (s/validate model/Categoria eletronicos))
(testa-schema)

(db/cria-dados-de-exemplo! conn)


(def categorias (db/todas-as-categorias (d/db conn)))
(pprint categorias)
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

produtos
