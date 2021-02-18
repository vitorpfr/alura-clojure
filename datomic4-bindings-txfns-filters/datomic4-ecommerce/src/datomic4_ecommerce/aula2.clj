(ns datomic4-ecommerce.aula2
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
(pprint primeiro)

; atual: (db/atualiza-preco conn (:produto/id primeiro) 20M)
; duas pessoas ao mesmo tempo:
;(db/atualiza-preco conn (:produto/id primeiro) 30M)
;(db/atualiza-preco conn (:produto/id primeiro) 31M)

; problema: 2 pessoas alterarem ao mesmo tempo (ex: uma quer somar 10 e a outra 11)
; se as duas se basearem no valor antigo ao mesmo tempo, a segunda vai somar 1 ao invés de 11
; (problema similar ao que o atom resolve em clojure) - atomicity
; datom também tem maneiras de lidar com concurrency!

(pprint primeiro)
(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 2500.10M 2600.10M))
(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 2600.10M 2700M))
;(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 2600.10M 2800M)) ; nao funciona pq o valor antigo não é igual ao que tá no db

; essa forma abaixo é ruim pq perdemos validação de schema (cada lista tem um tipo diferente)
;(db/atualiza-produto conn [:produto/preco 20M 30M] [:produto/slug :a :b])

; alternativa generalizar e passar produto antigo e produto novo:


(def segundo (second produtos))
(pprint segundo)
(def a-atualizar {:produto/id      (:produto/id segundo)
                  :produto/preco   10000M
                  :produto/estoque 5})

(pprint @(db/atualiza-produto! conn segundo a-atualizar))

; não deveria conseguir executar na segunda vez! pq o valor "antigo" não é mais igual ao valor atual
(pprint @(db/atualiza-produto! conn segundo a-atualizar))