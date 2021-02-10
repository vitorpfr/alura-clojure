(ns datomic-1-ecommerce.aula1
  (:use clojure.pprint)
  (:require [datomic-1-ecommerce.db :as db]
            [datomic-1-ecommerce.model :as m]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

(let [computador (m/novo-produto "Computador Novo" "/computador_novo" 2500.10M)]
  (db/transaciona conn [computador]))

; estrutura de um datom
;#datom [id-da-entidade atributo valor id-da-tx added?]

; adicionando schema
;{:status :ready,
; :val {:db-before datomic.db.Db,
;       @7274275c :db-after,
;       datomic.db.Db @30552c4,
;       :tx-data [#datom[13194139534312 50 #inst"2021-02-08T20:32:23.961-00:00" 13194139534312 true] ; registrando a transação 13194139534312
;                 #datom[72 10 :produto/nome 13194139534312 true] ; registrando o db/ident
;                 #datom[72 40 23 13194139534312 true]       ; registrando o db/valueType (40) na entidade 72 com o valor string (23)
;                 #datom[72 41 35 13194139534312 true]       ; registrando o db/cardinality (41) na entidade 72 com o valor one (35)
;                 #datom[72 62 "O nome de um produto" 13194139534312 true] ; registrando o db/doc (62) na entidade 72
;                 #datom[73 10 :produto/slug 13194139534312 true]
;                 #datom[73 40 23 13194139534312 true]
;                 #datom[73 41 35 13194139534312 true]
;                 #datom[73 62 "O caminho para acessar esse produto via http" 13194139534312 true]
;                 #datom[74 10 :produto/preco 13194139534312 true]
;                 #datom[74 40 61 13194139534312 true]
;                 #datom[74 41 35 13194139534312 true]
;                 #datom[74 62 "O preço de um produto com precisão monetária" 13194139534312 true]
;                 #datom[0 13 72 13194139534312 true]
;                 #datom[0 13 73 13194139534312 true]
;                 #datom[0 13 74 13194139534312 true]],
;       :tempids {-9223301668109598132 72, -9223301668109598131 73, -9223301668109598130 74}}}

; 72 é produto/nome, 73 é produto/slug, 74 é produto/preco

; adicionando produto
;{:status :ready,
; :val {:db-before datomic.db.Db,
;       @255b0264 :db-after,
;       datomic.db.Db @f5b402a9,
;       :tx-data [#datom[13194139534313 50 #inst"2021-02-08T21:06:24.996-00:00" 13194139534313 true]
;                 #datom[17592186045418 72 "Computador Novo" 13194139534313 true] ; pra entidade de id 17592186045418, adicionei pro atributo nome (72) o valor "Computador Novo" na transacao 13194139534313
;                 #datom[17592186045418 73 "/computador_novo" 13194139534313 true]
;                 #datom[17592186045418 74 2500.10M 13194139534313 true]],
;       :tempids {-9223301668109598122 17592186045418}}}

; fazer queries
; no where vc coloca a estrutura de datom que vc quer que de match, com os 5 campos opcionais

; banco de dados somente pra leitura, no instante que essa linha é executada
(def db (d/db conn))

(d/q '[:find ?entity-id
       :where [?entity-id :produto/nome]]
     db)
; :produto/nome: é fixo
; ?entity: pode ser qualquer valor
; banco vai varrer todos os datoms do sistema e vai selecionar os datoms que tem esse padrão

(let [celular-caro (m/novo-produto "Celular Caro" "/celular" 8888.10M)]
  (db/transaciona conn [celular-caro]))

; tirando uma nova fotografia (SNAPSHOT) do banco
(def db (d/db conn))
(d/q '[:find ?entity-id
       :where [?entity-id :produto/nome]]
     db)