(ns datomic4-ecommerce.aula5
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

;(dotimes [n 10] (db/visualizacao! conn (:produto/id ultimo)))
;(pprint (db/um-produto (d/db conn) (:produto/id ultimo)))

; o ideal seria mover esse código (que verifica o valor atual) pro transactor

; exemplo de funcao programatica criado pro datomic
(def ola
  (d/function '{:lang   :clojure
                :params [nome]
                :code   (str "Olá " nome)}))

(def db-adds
  (d/function '{:lang   :clojure
                :params [entidade valor]
                :code   [[:db/add entidade :produto/visualizacoes valor]]}))

; outra opcao
;(def db-adds
;  #db/fn {:lang :clojure
;          :params [entidade valor]
;          :code [[:db/add entidade :produto/visualizacoes valor]]})

(def incrementa-visualizacao
  #db/fn {:lang   :clojure
          :params [db produto-id]
          :code   (let [visualizacoes (d/q '[:find ?visualizacoes .
                                             :in $ ?id
                                             :where
                                             [?produto :produto/id ?id]
                                             [?produto :produto/visualizacoes ?visualizacoes]]
                                           db produto-id)
                        atual (or visualizacoes 0)
                        total-novo (inc atual)]
                    [{:produto/id            produto-id
                      :produto/visualizacoes total-novo}])})

; instalar a funcao
(pprint @ (d/transact
           conn
           [{:db/doc   "Incrementa o atributo produto/visualizacoes de uma entidade"
             :db/ident :incrementa-visualizacao
             :db/fn    incrementa-visualizacao}]))
; problema: com isso estamos serializando o passo a passo a ser feito no transactor
; (pq ele so executa um por vez)
; mas pra casos de concurrency pode ser importante!

(dotimes [n 10] (db/visualizacao! conn (:produto/id ultimo)))
(pprint (db/um-produto (d/db conn) (:produto/id ultimo)))

; Uma transaction function permite rodar qualquer código no transactor do datomic. Isso dá um super poder mas...
; Isso diminui a velocidade de escrita do sistema como um todo.
; To do código que roda no transactor roda de forma serializada, enfileirando as transações. Portanto buscamos minimizar seu impacto com o mínimo de código necessário.

;O que aprendemos nessa aula:
;
;como criar uma função local e registrá-la no datomic
;como invocar uma transaction function customizada