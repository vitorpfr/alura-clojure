(ns ecommerce.aula4
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

(db/atribui-categoria! conn [computador celular celular-barato] eletronicos)
(db/atribui-categoria! conn [xadrez] esporte)

(def produtos (db/todos-os-produtos (d/db conn)))
produtos

(pprint (db/todos-os-nomes-de-produtos-e-categorias (d/db conn)))

(pprint (db/todos-os-produtos-da-categoria (d/db conn) "Eletrônicos"))

;Vimos nessa aula:
;
;como cruzar dados em uma query
;como trazer o db/id de uma referência
;como fazer uma query que acessa os dados de uma referência
;como fazer forward e backward navigation em um pull (conteudo no arquivo db)