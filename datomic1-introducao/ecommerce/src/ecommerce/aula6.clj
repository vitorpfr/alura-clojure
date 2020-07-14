(ns ecommerce.aula6
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

; abre conexão com banco
(def conn (db/abre-conexao))

; cria schema dentro do banco
@(db/cria-schema conn)

; insere vários produtos no banco
(let [computador (model/novo-produto "Computador Novo" "/computador-novo" 2500.10M)
      impressora (model/novo-produto "Impressora Nova" "/impressora" 999.990M)
      celular (model/novo-produto "Celular Caro" "/celular" 8888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato" "/celular-barato" 0.1M)]
  (pprint @(d/transact conn [computador impressora celular calculadora celular-barato])))



; traz 2 produtos
(db/todos-os-produtos-por-preco (d/db conn) 1000)

; traz só 1, o celular caro
(db/todos-os-produtos-por-preco (d/db conn) 5000)

; adiciiona duas palavras-chave
(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "desktop"]
                  [:db/add 17592186045418 :produto/palavra-chave "computador"]])


(pprint (db/todos-os-produtos (d/db conn)))


; remove uma das palavras-chave
(d/transact conn [[:db/retract 17592186045418 :produto/palavra-chave "computador"]])


(pprint (db/todos-os-produtos (d/db conn)))

(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "monitor preto e branco"]
                  [:db/add 17592186045420 :produto/palavra-chave "celular"]
                  [:db/add 17592186045422 :produto/palavra-chave "celular"]])

(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "celular")) ; retorna 2
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "computador")) ; retorna 0
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "monitor preto e branco")) ; retorna 1

;(db/apaga-banco)
;Aula 6
;Vimos nessa aula:
;
;definir um plano de ação para suas queries
;utilizar predicates para filtrar seu dados
;cardinalidade to many
;queries e adds em cardinalidade múltipla
