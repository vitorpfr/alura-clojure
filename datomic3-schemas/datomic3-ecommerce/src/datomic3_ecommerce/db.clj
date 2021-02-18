(ns datomic3-ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic3-ecommerce.model :as model]
            [schema.core :as s]
            [clojure.walk :as walk]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao! []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco! []
  (d/delete-database db-uri))

; Produtos
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco ponto flutuante 1 ==> 3500.10
; categoria_id integer ==> 3

; lista de datoms:
; id_entidade atributo valor tx_id op
; 15 :produto/nome Computador Novo     ID_TX     operacao
; 15 :produto/slug /computador_novo    ID_TX     operacao
; 15 :produto/preco 3500.10    ID_TX     operacao
; 15 :produto/categoria 37
; 17 :produto/nome Telefone Caro    ID_TX     operacao
; 17 :produto/slug /telefone    ID_TX     operacao
; 17 :produto/preco 8888.88    ID_TX     operacao

; 37 :categoria/nome "Eletronicos"

(def schema [
             ; Produtos
             {:db/ident       :produto/nome
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
              :db/cardinality :db.cardinality/many}
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}
             {:db/ident       :produto/estoque
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}
             {:db/ident       :produto/digital
              :db/valueType   :db.type/boolean
              :db/cardinality :db.cardinality/one}

             ; Categorias
             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}

             ; Transações
             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             ])

(def Produto
  {:nome          s/Str
   :slug          s/Str
   :preco         BigDecimal
   :id            java.util.UUID
   :palavra-chave [s/Str]})

; datomic.tx se refere ao id da transação atual
(s/defn adiciona-ou-altera-produtos!
  ([conn, produtos :- [model/Produto]]
   (d/transact conn produtos))
  ([conn, produtos :- [model/Produto], ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(defn cria-schema! [conn]
  (d/transact conn schema))

(defn dissoc-db-id [entidade]
  (if (map? entidade)
    (dissoc entidade :db/id)
    entidade))

(defn datomic-para-entidade [entidades]
  (walk/prewalk dissoc-db-id entidades))

; problema: o maybe permite nil
; nil permite nullpointerexception
; nullpointerexception permite um inferno de exceptions
; usamos maybe somente em retorno de função e somente qunado fizer muito sentido
; isto é... maybe não é usado em mapas. em mapas usamos as chaves opcionais
(s/defn um-produto :- (s/maybe model/Produto)
  [db, produto-id :- java.util.UUID]
  (let [resultado (d/pull db '[* {:produto/categoria [*]}] [:produto/id produto-id])
        produto (datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

; nesse caso não tem maybe pq vai retornar o produto ou erro
(s/defn um-produto! :- model/Produto
  [db, produto-id :- java.util.UUID]
  (if-let [produto (um-produto db produto-id)]
    produto
    (throw (ex-info "Não encontrei uma entidade" {:type :errors/not-found, :id produto-id}))))

(comment
  ; se eu quiser pegar a data de nascimento ea cidade de um usuario, através do id dele
  (d/pull db '[:usuario/data-de-nascimento :usuario/cidade] [:usuario/id usuario_id]))

(defn db-adds-de-atribuicao-de-categorias [produtos categoria]
  (reduce (fn [db-adds produto] (conj db-adds [:db/add
                                               [:produto/id (:produto/id produto)]
                                               :produto/categoria
                                               [:categoria/id (:categoria/id categoria)]]))
          []
          produtos))


(defn atribui-categoria! [conn produtos categoria]
  (let [a-transacionar (db-adds-de-atribuicao-de-categorias produtos categoria)]
    (d/transact conn a-transacionar)))


(s/defn adiciona-categorias! [conn categorias :- [model/Categoria]]
  (d/transact conn categorias))

; essa query abaixo é como um "join" feito no datomic!
(defn todos-os-nomes-de-produtos-e-categorias [db]
  (d/q '[:find ?prod-nome ?cat-nome
         :keys produto categoria
         :where [?produto :produto/nome ?prod-nome]
         [?produto :produto/categoria ?cat]
         [?cat :categoria/nome ?cat-nome]]
       db))




; [] em volta do pull é pra poder vir lista de elementos, e nao lista de listas de elementos
; mas qdo faz só isso, ele retorna só um por padrão
; pra retornar todos, mas como lista de elementos, usa-se :find [?val ...]
(s/defn todas-as-categorias :- [model/Categoria]
  [db]
  (datomic-para-entidade
    (d/q '[:find [(pull ?categoria [*]) ...]
           :where [?categoria :categoria/id]] db)))

; pull - map specification (navegar dentro da categoria)
(s/defn todos-os-produtos :- [model/Produto] [db]
  (datomic-para-entidade
    (d/q '[:find [(pull ?entidade [* {:produto/categoria [*]}]) ...]
           :where [?entidade :produto/nome]] db)))


(defn cria-dados-de-exemplo! [conn]
  (def eletronicos (model/nova-categoria "Eletrônicos"))
  (def esporte (model/nova-categoria "Esporte"))

  (adiciona-categorias! conn [eletronicos, esporte])

  (def computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M, 10))
  (def celular (model/novo-produto "Celular Caro", "/celular", 888888.10M))
  ;(def calculadora {:produto/nome "Calculadora com 4 operações"})
  (def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))
  (def xadrez (model/novo-produto (model/uuid) "Tabuleiro de xadrez" "/tabuleiro-de-xadrez" 30M, 5))
  (def jogo (assoc (model/novo-produto (model/uuid) "Jogo online" "/jogo-online" 20M) :produto/digital true))

  (adiciona-ou-altera-produtos! conn [computador, celular, celular-barato xadrez jogo] "200.216.222.125")

  (atribui-categoria! conn [computador celular celular-barato jogo] eletronicos)
  (atribui-categoria! conn [xadrez] esporte))

; regra estoque existe 2x: ele dá match se encaixaar em uma regra ou outra
(def regras
  '[
    [(estoque ?produto ?estoque) [?produto :produto/estoque ?estoque]]
    [(estoque ?produto ?estoque) [?produto :produto/digital true] [(ground 100) ?estoque]]
    [(pode-vender? ?produto) (estoque ?produto ?estoque) [(> ?estoque 0)]]
    ])

(s/defn todos-os-produtos-vendaveis :- [model/Produto] [db]
  (datomic-para-entidade
    (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
           :in $ %
           :where (pode-vender? ?produto)]
         db regras)))

; tá retornando uma lista de elementos ao inves do elemento direto
; problema: find spec (ver find specifications em https://docs.datomic.com/on-prem/query/query.html)
; lá tem todos os formatos de :find (o que retora lista de lista, o que retorna lista, o que retorna o elemento direto, etc)
(s/defn um-produto-vendavel :- (s/maybe model/Produto)
  [db, produto-id :- java.util.UUID]
  (let [query '[:find (pull ?produto [* {:produto/categoria [*]}]) .
                :in $ % ?id
                :where
                [?produto :produto/id ?id]
                (pode-vender? ?produto)]
        resultado (d/q query db regras produto-id)
        produto (datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))