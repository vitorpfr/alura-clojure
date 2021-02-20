(ns datomic6-ecommerce.aula4
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

(defn extrai-nome-da-chave [chave]
  (cond
    (keyword? chave) chave
    (instance? schema.core.OptionalKey chave) (get chave :k)
    :else chave))

(defn propriedades-do-valor [valor]
  (if (vector? valor)
    (merge {:db/cardinality :db.cardinality/many}
           (propriedades-do-valor (first valor)))
    (cond
      (= valor java.util.UUID) {:db/valueType :db.type/uuid
                                :db/unique    :db.unique/identity}
      (= valor s/Str) {:db/valueType :db.type/string}
      (= valor BigDecimal) {:db/valueType :db.type/bigdec}
      (= valor Long) {:db/valueType :db.type/long}
      (= valor s/Bool) {:db/valueType :db.type/boolean}
      (map? valor) {:db/valueType :db.type/ref}
      :else {:db/valueType (str "desconhecido: " valor)})))

(defn chave-valor-para-definicao [[chave valor]]
  ;(println chave)
  (let [base {:db/ident       (extrai-nome-da-chave chave)
              :db/cardinality :db.cardinality/one}
        extra (propriedades-do-valor valor)
        schema-do-datomic (merge base extra)]
    schema-do-datomic))

(defn schema-to-datomic [definicao]
  (mapv chave-valor-para-definicao definicao))

(schema-to-datomic model/Categoria)
(schema-to-datomic model/Variacao)
(schema-to-datomic model/Venda)
(schema-to-datomic model/Produto)

;O que aprendemos nessa aula:
;
;As dificuldades de lidar com dois schemas
;A similaridade entre schemas
;Como criar um gerador de schemas
;Como lidar com referências para outros schemas
;Como suportar coleções