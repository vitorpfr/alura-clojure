(ns ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

(db/apaga-banco!)
(def conn (db/abre-conexao!))

(db/cria-schema! conn)

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esporte (model/nova-categoria "Esporte"))

(db/adiciona-categorias! conn [eletronicos, esporte])

(def categorias (db/todas-as-categorias (d/db conn)))
(pprint categorias)

(def computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M))
(def celular (model/novo-produto "Celular Caro", "/celular", 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))
(def xadrez (model/novo-produto "Tabuleiro de xadrez" "/tabuleiro-de-xadrez" 30.00M))

(db/adiciona-produtos! conn [computador, celular, calculadora, celular-barato xadrez])

(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

; quero acrescentar a categoria do produto
; E - [:produto/id (:produto/id computador)] - isso é um lookup ref pro id do computador
; A - :produto/categoria
; V - [:categoria/id (:categoria/id eletronicos)] - lookup ref pro id da categoria eletronicos
;(pprint @(d/transact conn [[:db/add
;                            [:produto/id (:produto/id computador)]
;                            :produto/categoria
;                            [:categoria/id (:categoria/id eletronicos)]]]))

; confirmar que funcionou
;(db/um-produto (d/db conn) (:produto/id computador))
; aqui ele mostra o produto já com a categoria como um numero!


; adicionando a categoria de eletronicos em 3 produtos de uma vez
(db/atribui-categoria! conn [computador celular celular-barato] eletronicos)
; adicionando
(db/atribui-categoria! conn [xadrez] esporte)

(def produtos (db/todos-os-produtos (d/db conn)))
produtos

; Para uma entidade referenciar outra entidade no banco:
; Uma referência a outra entidade é db.type/ref

;Vimos nessa aula:

;a importância de refatorar nosso código
;a referência entre entidades, permitindo um relacionamento um para um ou muitos para um