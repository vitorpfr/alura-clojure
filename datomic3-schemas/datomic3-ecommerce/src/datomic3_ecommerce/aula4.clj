(ns datomic3-ecommerce.aula4
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

(db/todos-os-produtos-vendaveis (d/db conn))

(def produtos (db/todos-os-produtos (d/db conn)))

(defn verifica-se-pode-vender [produto]
  (pprint "Analisando um produto")
  (pprint (:produto/estoque produto))
  (pprint (:produto/digital produto))
  (pprint (db/um-produto-vendavel (d/db conn) (:produto/id produto)))
  )

(map verifica-se-pode-vender produtos)


(pprint (db/um-produto-vendavel (d/db conn) (:produto/id (first produtos))))
(pprint (db/um-produto-vendavel (d/db conn) (:produto/id (second produtos))))