(ns datomic6-ecommerce.db.venda
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic6-ecommerce.model :as model]
            [datomic6-ecommerce.db.entidade :as db.entidade]
            [schema.core :as s]
            [clojure.set :as cset]))

(s/defn adiciona!
  [conn produto-id quantidade]
  (let [id (model/uuid)]
    (d/transact conn [{:db/id            "venda"
                       :venda/id         id
                       :venda/produto    [:produto/id produto-id]
                       :venda/quantidade quantidade
                       :venda/situacao   "nova"}])
    id))

; minha versão da fn 'custo'
;(defn custo [db venda-id]
;  (let [venda (d/pull db '[* {:venda/produto [*]}] [:venda/id venda-id])]
;    (* (-> venda :venda/produto :produto/preco)
;       (:venda/quantidade venda))))

; versão final - ainda com problema pq pega o custo atual, e nao do instante da venda
;(defn custo [db venda-id]
;  (d/q '[:find (sum ?preco-por-produto) .
;         :in $ ?id
;         :where
;         [?venda :venda/id ?id]
;         [?venda :venda/quantidade ?quantidade]
;         [?venda :venda/produto ?produto]
;         [?produto :produto/preco ?preco]
;         [(* ?preco ?quantidade) ?preco-por-produto]]
;       db venda-id))

(defn instante-da-venda [db venda-id]
  (d/q '[:find ?instante .
         :in $ ?id
         :where
         [_ :venda/id ?id ?tx true]
         [?tx :db/txInstant ?instante]]
       db venda-id))

; versão final - correta
; executamos em duas queries, poderiamos executar em uma só fazendo junções com nested queries ou novas condições
(defn custo [db venda-id]
  (d/q '[:find (sum ?preco-por-produto) .
         :in $ ?id
         :where
         [?venda :venda/id ?id]
         [?venda :venda/quantidade ?quantidade]
         [?venda :venda/produto ?produto]
         [?produto :produto/preco ?preco]
         [(* ?preco ?quantidade) ?preco-por-produto]]
       (d/as-of db (instante-da-venda db venda-id)) venda-id))

; possivel problema ainda existe: e se a quantidade da venda mudar no futuro? essa função vai retornar o custo inicial ainda

(defn cancela!-old [conn venda-id]
  (d/transact conn [[:db/retractEntity [:venda/id venda-id]]]))

(defn ativas [db]
  (db.entidade/datomic-para-entidade
    (d/q '[:find ?id
           :where
           [?v :venda/id ?id]
           [?v :venda/situacao ?situacao]
           [(not= ?situacao "cancelada")]] db)))

; d/history olha pra todos os datoms, não só os que estão valendo agora
(defn todas [db]
  (db.entidade/datomic-para-entidade
    (d/q '[:find ?id
           :where [_ :venda/id ?id]]
         db)))

(defn canceladas [db]
  (db.entidade/datomic-para-entidade
    (d/q '[:find ?id
           :where
           [?v :venda/id ?id]
           [?v :venda/situacao "cancelada"]]
         db)))

(defn altera-situacao! [conn venda-id situacao]
  (d/transact conn [{:venda/id       venda-id
                     :venda/situacao situacao}]))

(defn historico [db venda-id]
  (->> (d/q '[:find ?instante ?situacao
              :in $ ?id
              :where
              [?venda :venda/id ?id]
              [?venda :venda/situacao ?situacao ?tx true]
              [?tx :db/txInstant ?instante]]
            (d/history db) venda-id)
       (sort-by first)))

(defn cancela! [conn venda-id]
  (altera-situacao! conn venda-id "cancelada"))

; aqui conseguimos fazer um join entre dois snapshots diferentes do banco
; um atual (db) e um "since" o instante passado (db-filtrado)
(defn historico-geral [db instante-desde]
  (let [db-filtrado (d/since db instante-desde)]
    (->> (d/q '[:find ?id ?instante ?situacao
                :in $ $filtrado
                :where
                [$ ?venda :venda/id ?id]
                [$filtrado ?venda :venda/situacao ?situacao ?tx true]
                [$filtrado ?tx :db/txInstant ?instante]]
              db db-filtrado)
         (sort-by first))))

; utilidade do since: em sistemas distribuidos, é comum queremos fazer sincronizações entre sistemas
; pra sincronizar dados, é comum fazer queries de "delta" (mudanças desde o instante x)
; nesse caso podemos usar essa combinação de snapshot atual e "since"

;O que aprendemos nessa aula:
;
;Como utilizar atributos para representar situações
;A dificuldade de usar since puro
;Como fazer o join de filtros
;Quando usar join de filtros
