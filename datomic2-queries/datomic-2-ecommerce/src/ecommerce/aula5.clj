(ns ecommerce.aula5
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
(def xadrez (model/novo-produto "Tabuleiro de xadrez" "/tabuleiro-de-xadrez" 30M))

(db/adiciona-produtos! conn [computador, celular, calculadora, celular-barato xadrez])

(db/atribui-categoria! conn [computador celular celular-barato] eletronicos)
(db/atribui-categoria! conn [xadrez] esporte)

(def produtos (db/todos-os-produtos (d/db conn)))
produtos

; adicionando um produto novo com uma categoria nova
(pprint @(db/adiciona-produtos! conn [{:produto/nome      "Camiseta"
                                       :produto/slug      "/camiseta"
                                       :produto/preco     30M
                                       :produto/id        (model/uuid)
                                       :produto/categoria {:categoria/nome "Roupas"
                                                           :categoria/id   (model/uuid)}}]))


(db/todos-os-produtos (d/db conn))
(db/todos-os-nomes-de-produtos-e-categorias (d/db conn))

; e se a categoria já existe?
; pode fazer um lookup com o uuid
(pprint @(db/adiciona-produtos! conn [{:produto/nome      "Dama"
                                       :produto/slug      "/dama"
                                       :produto/preco     15M
                                       :produto/id        (model/uuid)
                                       :produto/categoria [:categoria/id (:categoria/id esporte)]}]))

(db/todos-os-nomes-de-produtos-e-categorias (d/db conn))

; conclusão: podemos fazer transacionar nested maps e adicioanr mais de uma entidade
; mas se a categoria já existir, podemos pegar a categoria via lookup ref do uuid

;; AGGREGATES WHEN QUERYING (min, max, count)

(pprint (db/resumo-dos-produtos (d/db conn)))

;; GROUPING BY BEFORE AGGREGATING
(pprint (db/resumo-dos-produtos-por-categoria (d/db conn)))

;Vimos nessa aula:
;
;como adicionar entidades referenciadas em uma única transação
;o uso de aggregates
;o uso do with na agregação