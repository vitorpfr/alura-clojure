(ns datomic4-ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic4-ecommerce.db :as db]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)
(db/cria-dados-de-exemplo! conn)

(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro (first produtos))
(def segundo (second produtos))
(def ultimo (last produtos))
(pprint ultimo)


(pprint @(db/adiciona-variacao! conn (:produto/id ultimo) "Season pass" 40M))
(pprint @(db/adiciona-variacao! conn (:produto/id ultimo) "Season pass 4 anos" 60M))

(d/q '[:find (pull ?produto [*])
       :where [?produto :produto/nome]]
     (d/db conn))

(db/todos-os-produtos (d/db conn))
(pprint (db/total-de-produtos (d/db conn)))

(db/remove-produto! conn (:produto/id ultimo))
(db/todos-os-produtos (d/db conn))
(pprint (db/total-de-produtos (d/db conn)))

(pprint (d/q '[:find ?nome
               :where [_ :variacao/nome ?nome]]
             (d/db conn)))

;O que aprendemos nessa aula:
;
;o que são os temp ids
;como usar temp ids para criar relacionamentos entre entidades
;como utilizar componentes similares ao efeito de "auto cascade" no sql
;como utilizar o retractEntity para remover completamente uma entidade