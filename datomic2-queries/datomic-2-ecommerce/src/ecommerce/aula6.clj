(ns ecommerce.aula6
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

(db/adiciona-produtos! conn [computador, celular, calculadora, celular-barato xadrez] "200.216.222.125")

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
                                                           :categoria/id   (model/uuid)}}] "20.216.222.12"))

(db/todos-os-produtos (d/db conn))

(db/todos-os-produtos-mais-caros (d/db conn))
(db/todos-os-produtos-mais-baratos (d/db conn))
(db/todos-os-produtos-do-ip (d/db conn) "200.216.222.125")  ; 4 produtos
(db/todos-os-produtos-do-ip (d/db conn) "20.216.222.12")    ; 1 produto
(db/todos-os-produtos-do-ip (d/db conn) "20.216.2.12")      ; nenhum produto

;Vimos nessa aula:
;
;a importância e como usar nested queries
;como utilizar transações para armazenar dados
;como fazer queries em suas transações