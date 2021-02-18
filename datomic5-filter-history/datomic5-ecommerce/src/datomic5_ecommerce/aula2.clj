(ns datomic5-ecommerce.aula2
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

; problema: pra calcular o custo, a gente tá usando o preço atual do produto (que pode ser alterado dps!)
; o certo é usarmos o preço do momento da venda!
(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))

;O que aprendemos nessa aula:
;
;Criar um novo modelo para representar vendas
;Criar queries complexas com mais de uma entidade e agregação
;Entender o problema de atualização com o tempo