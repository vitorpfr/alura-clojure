(ns datomic-1-ecommerce.db
  (:require [datomic.api :as d]))

; como conectar ao datomic: rodar no terminal, estando na pasta do datomic:
; bin/transactor config/dev-transactor-template.properties

; create db
(def db-uri "datomic:dev://localhost:4334/ecommerce")
(d/create-database db-uri)

; connect to db
(def conn (d/connect db-uri))

; delete db
;(d/delete-database db-uri)

; custom db fns
(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))


; Produtos - schema
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco bigdec 1 ==> 3500.10

; em sql, isso seria uma tabela com 4 colunas (id, nome, slug, preço)
; em datomic, isso seria um tabelão com uma linha pra cada atributo
; (ou seja, cada entrada dessa tabela tem (id, atributo, valor))
; id_entidade atributo valor tx_id op
; 15 :produto/nome "Computador Novo" 142521512512 true
; 15 :produto/slug "computador_novo"
; 15 :produto/preco 3500.10
; 17 :produto/nome "Telefone"
; 17 :produto/slug "/telefone"
; 17 :produto/preco 8888.88

(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}

             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}

             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preço de um produto com precisão monetária"}])


(defn cria-schema [conn]
  (d/transact conn schema))

(defn transaciona [conn obj]
  (d/transact conn obj))

(defn todos-os-produtos [db]
  (d/q '[:find ?entity-id
         :where [?entity-id :produto/nome]] db))

(defn todos-os-produtos-por-slug-fixo [db]
  (d/q '[:find ?entidade
         :where [?entidade :produto/slug "/computador-novo"]]
       db))

; passando parâmetro pra query:
; no :in, ou eu não coloco nada (e ele assume que o unico arg é o db), ou eu coloco db e os args
; por padrão, o banco de dados é o $
; com o in, ?slug-a-ser-buscado está "binded" com o valor recebido pela fn (valor fixo)
(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-a-ser-buscado                          ; importante simbolo da query ter nome diferente de simbolo da funcao
         :where [?entidade :produto/slug ?slug-a-ser-buscado]]
       db slug))

; ?entity => ?e => ?product => ?p (nesse caso _ pq nao usamos)
(defn todos-os-slugs [db]
  (d/q '[:find ?slug
         :where [_ :produto/slug ?slug]]
       db))

; busca com mais de uma info
; quando que quero mais de uma info, preciso ligar elas pelo simbolo ?e senao ele vai retornar todas as combinacoes de nome e preco
(defn todos-os-produtos-por-preco-old [db]
  (d/q '[:find ?nome ?preco
         :where [?e :produto/preco ?preco]
                [?e :produto/nome ?nome]]
       db))

; se quisermos que saia um mapa ao inves de vetores no resultado da query?
(defn todos-os-produtos-por-preco [db]
  (d/q '[:find ?nome ?preco
         :keys produto/nome, produto/preco
         :where [?e :produto/preco ?preco]
         [?e :produto/nome ?nome]]
       db))
; dai o resultado vem como
;[{:nome "Celular Caro", :preco 8888.10M}
; {:nome "Celular Barato", :preco 0.1M}
; {:nome "Computador Novo", :preco 2500.10M}]
; nesse caso estou sendo explícito nos campos 1 a 1

; outra forma: pull
; pull explicito atributo a atributo
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entity-id [:produto/nome :produto/preco :produto/slug])
         :where [?entity-id :produto/nome]] db))

; pull generico (todos os atributos, inclusive o :db/id de cada entidade ou produto)
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entity-id [*])
         :where [?entity-id :produto/nome]] db))

; se quisermos especificar um preço minimo?
(defn todos-os-produtos-por-preco-minimo-errado [db preco-minimo]
  (d/q '[:find ?nome ?preco
         :in $ ?preco-minimo-a-ser-filtrado
         :keys produto/nome, produto/preco
         :where [?prod :produto/preco ?preco]
                [?prod :produto/nome ?nome]
                [(> ?preco ?preco-minimo-a-ser-filtrado)]]
       db preco-minimo))

; no datomic a gente que tem que especificar o plano de ação!
; por isso é importante ordenar as queries com as condições mais restritivas primeiro
; pra otimizar a query total!!

; na query acima: o ideal é filtrar o preço antes de pegar o nome, pra pegar o nome só das entidades que interessam
; conforme abaixo:
(defn todos-os-produtos-por-preco-minimo [db preco-minimo]
  (d/q '[:find ?nome ?preco
         :in $ ?preco-minimo-a-ser-filtrado
         :keys produto/nome, produto/preco
         :where [?prod :produto/preco ?preco]
                [(> ?preco ?preco-minimo-a-ser-filtrado)]
                [?prod :produto/nome ?nome]]
       db preco-minimo))


; novo schema: agora com palavra chave
(def new-schema [{:db/ident       :produto/nome
                  :db/valueType   :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc         "O nome de um produto"}

                 {:db/ident       :produto/slug
                  :db/valueType   :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc         "O caminho para acessar esse produto via http"}

                 {:db/ident       :produto/preco
                  :db/valueType   :db.type/bigdec
                  :db/cardinality :db.cardinality/one
                  :db/doc         "O preço de um produto com precisão monetária"}

                 {:db/ident       :produto/palavra-chave
                  :db/valueType   :db.type/string
                  :db/cardinality :db.cardinality/many
                  :db/doc         "Palavras-chave do produto"}])

(defn cria-novo-schema [conn]
  (d/transact conn new-schema))


(defn todos-os-produtos-por-palavra-chave [db palavra-chave-buscada]
  (d/q '[:find (pull ?prod [*])
         :in $ ?palavra-chave
         :where [?prod :produto/palavra-chave ?palavra-chave]]
       db palavra-chave-buscada))