(ns datomic5-ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic5-ecommerce.db.config :as db.config]
            [datomic5-ecommerce.db.produto :as db.produto]
            [datomic5-ecommerce.db.venda :as db.venda]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(def produtos (db.produto/todos (d/db conn)))
(def primeiro (first produtos))
(def ultimo (last produtos))

ultimo

(def venda1 (db.venda/adiciona! conn (:produto/id ultimo) 3))
(def venda2 (db.venda/adiciona! conn (:produto/id ultimo) 4))

(pprint venda1)

(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))

(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id ultimo)
                                                :produto/preco 300M}]))

(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))


; possivel alternativa: armazenar junto com a venda um :venda/preco, que é o preço que aquele produto foi vendido
; mas é uma ideia ruim pq podemos precisar de qq info do produto no momento da venda, ia gerar muita info duplicada

; solução: filtrar o banco no momento da venda, usando asOf
; passo 1: pegar a transação onde ocorreu a venda
(d/q '[:find ?tx .
       :in $ ?id
       :where
       [_ :venda/id ?id ?tx true]]
     (d/db conn) venda1)

; passo 2: pegar os atributos e valores da transação onde ocorreu a venda
(d/q '[:find ?nome-atributo ?valor
       :in $ ?id
       :where
       [_ :venda/id ?id ?tx true]
       [?tx ?atributo ?valor]
       [?atributo :db/ident ?nome-atributo]]
     (d/db conn) venda1)

; a entidade 50 tem o :db/ident de :db/txInstant
(d/q '[:find ?valor
       :where
       [50 :db/ident ?valor]]
     (d/db conn))

; agora podemos fazer a query pra pegar o instante da transação!!
(d/q '[:find ?instante .
       :in $ ?id
       :where
       [_ :venda/id ?id ?tx true]
       [?tx :db/txInstant ?instante]]
     (d/db conn) venda1)

; agora funciona pq o custo pega o instante exato que a venda ocorreu!
(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))

; problema: indices do datomic estão atualizados para o presente
; fazer queries no passado pode ser mais lento
; em alguns casos copiar o valor pode ser interessante

;O que aprendemos nessa aula:
;
;Como usar filtros em seu banco
;Como usar o as-of para buscar dados em um snapshot antigo do banco
;Como utilizar o as-of no dia a dia