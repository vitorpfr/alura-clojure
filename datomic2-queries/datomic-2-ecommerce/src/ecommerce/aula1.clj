(ns ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

(def conn (db/abre-conexao!))

(db/cria-schema! conn)

(let [computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto "Celular Caro", "/celular", 888888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)]
  (pprint @(d/transact conn [computador, celular, calculadora, celular-barato])))

(pprint (db/todos-os-produtos (d/db conn)))

(db/apaga-banco!)

; datomic console: UI mais user-friendly pra explorar um datomic, ver entidades, fazer queries, etc
; estando na pasta do datomic, rodar: bin/console -p 8080 dev datomic:dev://localhost:4334/

; como pegar os dados de um produto (entidade)?
; nesse caso quero fazer só um pull, sem nenhum tipo de where
(pprint (db/um-produto-por-dbid (d/db conn) 17592186045418))

; outra forma de buscar por um produto
(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro-dbid (-> produtos
                       ffirst
                       :db/id))
(println "O dbid do primeiro produto é" primeiro-dbid)
(pprint (db/um-produto-por-dbid (d/db conn) primeiro-dbid))

; ter id sequencial é ruim pq hackers podem tirar conclusões sobre ele (se num for maior, é mais recente)

; o ideal é usar outros tipos de id, ex: uuid
; ele é gerado aleatoriamente
; boa prática: usar uuid mesmo que o que está sendo modelado ja tenha um id unico (ex: cpf)

; agora que temos o id do produto, podemos fazer a busca pelo id dele (ao invés de usar o id do banco)

; pegando por produto-id
(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro-produto-id (-> produtos
                             ffirst
                             :produto/id))
(println "O produto id do primeiro produto é" primeiro-produto-id)
(pprint (db/um-produto (d/db conn) primeiro-produto-id))

; essa busca por identificador unico (e não db/id) é chamada de lookup ref na documentação