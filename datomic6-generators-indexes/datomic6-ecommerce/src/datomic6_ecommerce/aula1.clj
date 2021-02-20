(ns datomic6-ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic6-ecommerce.db.config :as db.config]
            [datomic6-ecommerce.db.produto :as db.produto]
            [datomic6-ecommerce.db.venda :as db.venda]
            [schema.core :as s]
            [datomic6-ecommerce.model :as model]
            [schema-generators.generators :as g]
            [datomic6-ecommerce.db.generators :as generators]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(db.produto/todos (d/db conn))

; g/sample to generate more data
(g/sample 20 model/Categoria)

(g/sample 10 model/Variacao generators/leaf-generators)
(g/sample 100 model/Variacao generators/leaf-generators)

;O que aprendemos nessa aula:
;
;Como criar seu próprio LeafGenerator
;Como customizar seus generators
;Organizar e padronizar nossos generators