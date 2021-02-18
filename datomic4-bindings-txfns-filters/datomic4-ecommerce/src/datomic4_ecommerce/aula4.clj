(ns datomic4-ecommerce.aula4
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

(dotimes [n 10] (db/visualizacao! conn (:produto/id ultimo)))
(pprint (db/um-produto (d/db conn) (:produto/id ultimo)))

; Quando devemos desativar o history de um atributo?
; Quando o conteúdo tem uma combinação de frequência e tamanho muito grande, ao mesmo tempo sendo um conteúdo que não precisamos do histórico.

;O que aprendemos nessa aula:
;
;como desativar history para um atributo de mudança frequente
;como utilizar um read+write para criar incrementos