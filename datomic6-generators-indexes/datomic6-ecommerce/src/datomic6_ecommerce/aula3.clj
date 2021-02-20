(ns datomic6-ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic6-ecommerce.db.config :as db.config]
            [datomic6-ecommerce.db.produto :as db.produto]
            [datomic6-ecommerce.db.venda :as db.venda]
            [schema.core :as s]
            [datomic6-ecommerce.model :as model]
            [schema-generators.generators :as g]
            [datomic6-ecommerce.db.generators :as generators]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(db.produto/todos (d/db conn))

(defn gera-10000-produtos [conn]
  (dotimes [n 50]
    (let [produtos-gerados (g/sample 200 model/Produto generators/leaf-generators)]
      (println n (count @(db.produto/adiciona-ou-altera! conn produtos-gerados))))))

; demora pq tem muita string
; string é pesada (um numero grande ocupa 64 bits, uma string grande ocupa MBs)
(println "Geração de produtos:")
(time (gera-10000-produtos conn))

(println "Busca do mais caro:")
(time (dotimes [_ 100] (db.produto/busca-mais-caro (d/db conn))))
(println "Busca dos mais caros que:")
(time (dotimes [_ 100] (count (db.produto/busca-mais-caros-que (d/db conn) 50000M))))

; temos uma tabela com 1M de datoms
; teoricamente uma busca do mais caro passaria pelos 1M de datoms
; entretanto, o datomic tem estruturas que facilitam essa busca
; estrutura de otimização de busca: indexes

; datomic index: https://docs.datomic.com/on-prem/query/indexes.html
; Por padrão, os datoms são indexados em buscas por entidade-atributo-valor-tx ou atributo-entidade-valor-tx
; As outras sequências só são indexadas em casos especiais

; Por ex: nossa 'busca-mais-caro' é uma busca AV (atributo-valor), que só é otimizada pra atributos :db/unique




; Ideia: criar uma busca por preco pra ver como é uma query não otimizada com indice
(println "Busca por preco:")
(def preco-mais-caro (db.produto/busca-mais-caro (d/db conn)))
(time (dotimes [_ 100] (db.produto/busca-por-preco (d/db conn) preco-mais-caro)))
; está levando em média 200-300 ms
; vamos testar colocar o preco como index no schema do db (:db/index true)
; nesse caso o index AV vai ser utilizado agora (referencia ao link acima), pq o atributo é indexado
; com index, agora leva em média 100ms rodando várias vezes


; pergunta: pq as queries 2 e 3 foram otimizadas, mas a 1 não?
; na 1 (busca-mais-caro), o ?preco não tá bindeado - ou seja, é uma busca só por atributos!
; já na 2 e na 3, o preço está bindeado, logo tem uma busca atributo-valor (AV)
; na 3 em particular é mais otimizado ainda pq é só a busca AV

(println "Busca por preco e nome (nao otimizada)")
(time (dotimes [_ 100] (db.produto/busca-por-preco-e-nome-nao-otimizada (d/db conn) 1000M "com")))

; tá demorando 1 a 2 segundos pra executar essa query
; ordem atual: pega nome -> contains trecho -> filtra preco minimo
; problema dessa ordem é que o contains vai ser executado em todos os datoms de nome
; ideal é colocar mais restritivo primeiro (filtra preco -> pega nome -> contains trecho

(println "Busca por preco e nome (otimizada)")
(time (dotimes [_ 100] (db.produto/busca-por-preco-e-nome (d/db conn) 1000M "com")))
; performance da query foi de 1500 ms pra 41 ms!!!!!!!!

; resumo: as duas principais alavancas de otimizar queries são:
; indexar atributos específicos
; melhorar plano de ação (operações mais restritivos primeiro e operações mais lentas por ultimo)






;O que aprendemos nessa aula:
;
;Como buscar por preço específico
;A existência de 4 tipos de indíces no Datomic
;Como adicionar um índice próprio
;A importância de um plano de ação
;Como pensar na ordem das condições de uma query
;Os desafios de uma String
