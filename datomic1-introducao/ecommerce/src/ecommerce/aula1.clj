(ns ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))




; abre conexão com banco
(def conn (db/abre-conexao))

; cria schema dentro do banco
@(db/cria-schema conn)

; insere um valor no banco
(let [computador (model/novo-produto "Computador Novo" "/computador_novo" 2500.10M)
      impressora (model/novo-produto "Impressora Nova" "/impressora" 999.990M)]
  @(d/transact conn [computador impressora]))

; salvar banco no instante em que eu executo a linha (só leitura)
(def db (d/db conn))

; fazer query em datalog
(d/q '[:find ?entidade
       :where [?entidade :produto/nome]]
     db)

; insere um valor no banco
(let [celular (model/novo-produto "Celular Caro" "/celular" 8888.10M)]
  @(d/transact conn [celular]))


; tirando um novo snapshot do banco
(def db (d/db conn))

; fazer query em datalog
(d/q '[:find ?entidade
       :where [?entidade :produto/nome]]
     db)

; Aula 1
;Nessa aula vimos:
;
;o que são schemas
;como transacionar schemas
;como transacionar dados
;queries simples
;o banco e seus snapshots

; apaga banco - opcional pra resetar tudo
(db/apaga-banco)
