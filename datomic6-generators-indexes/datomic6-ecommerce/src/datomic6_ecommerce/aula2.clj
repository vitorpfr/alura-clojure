(ns datomic6-ecommerce.aula2
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

;O que aprendemos nessa aula:
;
;Pequenas incompatibilidades entre schemas da plumatic e do Datomic
;Como medir o tempo de execução de partes do programa
