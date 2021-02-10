(ns datomic-1-ecommerce.aula6-cont
  (:use clojure.pprint)
  (:require [datomic-1-ecommerce.db :as db]
            [datomic-1-ecommerce.model :as m]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)
; nesse caso não tem problema redefinir o schema com mais um atributo, pq os outros vao ficar inalterados
(db/cria-novo-schema conn)

; adicionar produtos
(let [computador (m/novo-produto "Computador Novo" "/computador-novo" 2500.10M)
      celular-caro (m/novo-produto "Celular Caro" "/celular" 8888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (m/novo-produto "Celular Barato" "/celular-barato" 0.1M)]
  (pprint @(db/transaciona conn [computador celular-caro calculadora celular-barato])))

; fazer query pra pegar todos os produtos
(db/todos-os-produtos (d/db conn))

; tem que trazer 2
(db/todos-os-produtos-por-preco-minimo (d/db conn) 1000)

; tem que trazer 1
(db/todos-os-produtos-por-preco-minimo (d/db conn) 5000)

; agora queremos adicionar palavras chave nos produtos
(d/transact conn [[:db/add 17592186045419 :produto/palavra-chave "desktop"]
                  [:db/add 17592186045419 :produto/palavra-chave "computador"]])

; agora o computador novo tem as palavras chave "computador" e "desktop"
(db/todos-os-produtos (d/db conn))

; agora queremos remover palavra chave
(d/transact conn [[:db/retract 17592186045419 :produto/palavra-chave "computador"]])

; agora o computador novo tem as palavras chave "desktop"
(db/todos-os-produtos (d/db conn))

; agora queremos adicionar palavras chave nos produtos
(d/transact conn [[:db/add 17592186045419 :produto/palavra-chave "monitor preto e branco"]])

; agora o computador novo tem as palavras chave "desktop" e "monitor preto e branco"
(db/todos-os-produtos (d/db conn))

; agora queremos adicionar palavras chave nos produtos
(d/transact conn [[:db/add 17592186045420 :produto/palavra-chave "celular"]
                  [:db/add 17592186045422 :produto/palavra-chave "celular"]])


(db/todos-os-produtos (d/db conn))

; agora quero buscar produtos por palavra-chave
; traz os dois celulares
(db/todos-os-produtos-por-palavra-chave (d/db conn) "celular")

; nao traz nada
(db/todos-os-produtos-por-palavra-chave (d/db conn) "computador")

; traz um computador
(db/todos-os-produtos-por-palavra-chave (d/db conn) "desktop")