(ns datomic6-ecommerce.db.produto
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic6-ecommerce.model :as model]
            [datomic6-ecommerce.db.entidade :as db.entidade]
            [schema.core :as s]
            [clojure.set :as cset]))

(s/defn adiciona-ou-altera!
  ([conn, produtos :- [model/Produto]]
   (d/transact conn produtos))
  ([conn, produtos :- [model/Produto], ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(s/defn um :- (s/maybe model/Produto)
  [db, produto-id :- java.util.UUID]
  (let [resultado (d/pull db '[* {:produto/categoria [*]}] [:produto/id produto-id])
        produto (db.entidade/datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn um! :- model/Produto
  [db, produto-id :- java.util.UUID]
  (if-let [produto (um db produto-id)]
    produto
    (throw (ex-info "Não encontrei uma entidade" {:type :errors/not-found, :id produto-id}))))

; se eu quiser pegar a data de nascimento ea cidade de um usuario, através do id dele
;(d/pull db '[:usuario/data-de-nascimento :usuario/cidade] [:usuario/id usuario_id])

(s/defn todos :- [model/Produto] [db]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?entidade [* {:produto/categoria [*]}]) ...]
           :where [?entidade :produto/id]] db)))

(def regras
  '[
    [(estoque ?produto ?estoque) [?produto :produto/estoque ?estoque]]
    [(estoque ?produto ?estoque) [?produto :produto/digital true] [(ground 100) ?estoque]]
    [(pode-vender? ?produto) (estoque ?produto ?estoque) [(> ?estoque 0)]]
    [(produto-na-categoria ?produto ?nome-da-categoria)
     [?categoria :categoria/nome ?nome-da-categoria]
     [?produto :produto/categoria ?categoria]]
    ])

(s/defn todos-os-vendaveis :- [model/Produto] [db]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
           :in $ %
           :where (pode-vender? ?produto)]
         db regras)))

(s/defn um-vendavel :- (s/maybe model/Produto)
  [db, produto-id :- java.util.UUID]
  (let [query '[:find (pull ?produto [* {:produto/categoria [*]}]) .
                :in $ % ?id
                :where
                [?produto :produto/id ?id]
                (pode-vender? ?produto)]
        resultado (d/q query db regras produto-id)
        produto (db.entidade/datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn todos-nas-categorias :- [model/Produto]
  [db categorias :- [s/Str]]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
           :in $ % [?nome-da-categoria ...]
           :where (produto-na-categoria ?produto ?nome-da-categoria)]
         db regras categorias)))

(s/defn todos-nas-categorias-e-digital :- [model/Produto]
  [db, categorias :- [s/Str], digital? :- s/Bool]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
           :in $ % [?nome-da-categoria ...] ?eh-digital?
           :where
           (produto-na-categoria ?produto ?nome-da-categoria)
           [?produto :produto/digital ?eh-digital?]]
         db regras categorias digital?)))

(defn todos-os-nomes-de-produtos-e-categorias [db]
  (d/q '[:find ?prod-nome ?cat-nome
         :keys produto categoria
         :where [?produto :produto/nome ?prod-nome]
         [?produto :produto/categoria ?cat]
         [?cat :categoria/nome ?cat-nome]]
       db))

(s/defn atualiza-preco!
  [conn,
   produto-id :- java.util.UUID,
   preco-antigo :- BigDecimal
   preco-novo :- BigDecimal]
  (d/transact conn [[:db/cas [:produto/id produto-id] :produto/preco preco-antigo preco-novo]]))

(s/defn atualiza!
  [conn, antigo :- model/Produto, a-atualizar :- model/Produto]
  (let [produto-id (:produto/id antigo)
        atributos (cset/intersection (set (keys antigo)) (set (keys a-atualizar)))
        atributos (disj atributos :produto/id)
        txs (map (fn [atributo] [:db/cas [:produto/id produto-id] atributo (get antigo atributo) (get a-atualizar atributo)]) atributos)]
    (d/transact conn txs)))

(defn total [db]
  (d/q '[:find (count ?produto) .
         :where [?produto :produto/id]]
       db))

(s/defn remove! [conn produto-id :- java.util.UUID]
  (d/transact conn [[:db/retractEntity [:produto/id produto-id]]]))

; o order by fica em memoria (diferente do sql que o order by é feito no servidor do banco)
(defn historico-de-precos [db produto-id]
  (->> (d/q '[:find ?instante ?preco
              :in $ ?id
              :where
              [?produto :produto/id ?id]
              [?produto :produto/preco ?preco ?tx true]
              [?tx :db/txInstant ?instante]]
            (d/history db) produto-id)
       (sort-by first)))

(defn busca-mais-caro [db]
  (d/q '[:find (max ?preco) .
         :where [_ :produto/preco ?preco]]
       db))

(defn busca-mais-caros-que [db preco-minimo]
  (d/q '[:find ?preco
         :in $ ?minimo
         :where
         [_ :produto/preco ?preco]
         [(>= ?preco ?minimo)]]
       db preco-minimo))

(defn busca-por-preco [db preco-buscado]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?produto [*]) ...]
           :in $ ?preco
           :where [?produto :produto/preco ?preco]]
         db preco-buscado)))

(defn busca-por-preco-e-nome-nao-otimizada [db preco nome]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?produto [*]) ...]
           :in $ ?preco-minimo ?trecho
           :where
           [?produto :produto/nome ?nome]
           [(.contains ?nome ?trecho)]
           [?produto :produto/preco ?preco-minimo]]
         db preco nome)))

(defn busca-por-preco-e-nome [db preco nome]
  (db.entidade/datomic-para-entidade
    (d/q '[:find [(pull ?produto [*]) ...]
           :in $ ?preco-exato ?trecho
           :where
           [?produto :produto/preco ?preco-exato]
           [?produto :produto/nome ?nome]
           [(.contains ?nome ?trecho)]]
         db preco nome)))