(ns datomic4-ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic4-ecommerce.model :as model]
            [schema.core :as s]
            [clojure.set :as cset]
            [clojure.walk :as walk]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao! []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco! []
  (d/delete-database db-uri))

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
             ; isComponent: true quando essas entidades de variacoes so fazem sentido relacionadas a essa entidade especifica de produto (não existe variação de multiplos produtos)
             ; a consequencia é que se eu deletar via :retractEntity uma entidade com :produto/variacao ligada a variacoes, todas as variacoes vao ser removidas junto
             ; se eu deletar atributo a atributo, isso não acontece
             {:db/ident       :produto/variacao
              :db/valueType   :db.type/ref
              :db/isComponent true
              :db/cardinality :db.cardinality/many}
             ; com noHistory setado como true, o datomic não armazena o historico desse atributo (pra economizar storage/memoria)
             {:db/ident       :produto/visualizacoes
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one
              :db/noHistory   true}

             ; Variacoes
             {:db/ident       :variacao/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :variacao/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :variacao/preco
              :db/valueType   :db.type/bigdec
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
    [(produto-na-categoria ?produto ?nome-da-categoria)
     [?categoria :categoria/nome ?nome-da-categoria]
     [?produto :produto/categoria ?categoria]]
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

; binding com coleção: [?a ...] (recebe lista) ao invés de ?a (recebe scalar)
; reference: https://docs.datomic.com/on-prem/query/query.html#bindings
; é equivalente ao where x in ('a', 'b') do SQL
(s/defn todos-os-produtos-nas-categorias :- [model/Produto]
  [db categorias :- [s/Str]]
  (datomic-para-entidade
    (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
           :in $ % [?nome-da-categoria ...]
           :where (produto-na-categoria ?produto ?nome-da-categoria)]
         db regras categorias)))

(s/defn todos-os-produtos-nas-categorias-e-digital :- [model/Produto]
  [db, categorias :- [s/Str], digital? :- s/Bool]
  (datomic-para-entidade
    (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
           :in $ % [?nome-da-categoria ...] ?eh-digital?
           :where
           (produto-na-categoria ?produto ?nome-da-categoria)
           [?produto :produto/digital ?eh-digital?]]
         db regras categorias digital?)))

(s/defn atualiza-preco-errado
  [conn,
   produto-id :- java.util.UUID,
   preco-antigo :- BigDecimal
   preco-novo :- BigDecimal]
  (if (= preco-antigo (:produto/preco (d/pull (d/db conn) [*] (:produto/id produto-id))))
    ; essa abordagem não funciona pq aqui pode ter uma escrita!!!
    ; logo, o valor lido acima não vai ser igual ao valor abaixo
    ; essa abordagem horrivel não garante atomicidade
    ; não garante o que queríamos :(
    ; precisamos de algo equivalente a atom/ref (compare-and-set)
    (d/transact conn {:produto/id produto-id, :produto/preco preco-novo})
    (throw (ex-info "Valor foi alterado entre leitura e escrita" {:type :errors/transaction-validation-error})))
  )

; solução: compare-and-swap do datomic
; https://docs.datomic.com/cloud/transactions/transaction-functions.html#db-cas
; :db/cas é uma função do transactor que recebe valor antigo e novo, e só troca se o valor atual for igual ao antigo

; cas: compare and swap
; [:produto/id produto-id] é um lookup ref que já pega o numero da entidade
(s/defn atualiza-preco!
  [conn,
   produto-id :- java.util.UUID,
   preco-antigo :- BigDecimal
   preco-novo :- BigDecimal]
  (d/transact conn [[:db/cas [:produto/id produto-id] :produto/preco preco-antigo preco-novo]]))

; voce poderia querar generalizar o framework pra
; jogar exception caso exista campo em a-atualizar que não existe em antigo
(s/defn atualiza-produto!
  [conn, antigo :- model/Produto, a-atualizar :- model/Produto]
  (let [produto-id (:produto/id antigo)
        atributos (cset/intersection (set (keys antigo)) (set (keys a-atualizar)))
        atributos (disj atributos :produto/id)
        txs (map (fn [atributo] [:db/cas [:produto/id produto-id] atributo (get antigo atributo) (get a-atualizar atributo)]) atributos)]
    (d/transact conn txs)))

; :db/id é uma referencia temporaria a entidade
(s/defn adiciona-variacao!
  [conn, produto-id :- java.util.UUID, variacao :- s/Str, preco :- BigDecimal]
  (d/transact conn [{:db/id          "variacao-temporaria"
                     :variacao/nome  variacao
                     :variacao/preco preco
                     :variacao/id    (model/uuid)}

                    {:produto/id       produto-id
                     :produto/variacao "variacao-temporaria"}]))

(defn total-de-produtos [db]
  (d/q '[:find (count ?produto) .
         :where [?produto :produto/id]]
       db))

; poderia usar [:db/retract] na hora de transacionar
; o problema é que eu teria que passar cada atributo a ser removido - e se eu esquecer de algum?
; solucao: usar :retractEntity
; como nao tenho o id do banco (só o id do produto), eu faço um lookup ref [:produto/id produto-id]
(s/defn remove-produto! [conn produto-id :- java.util.UUID]
  (d/transact conn [[:db/retractEntity [:produto/id produto-id]]])
  )

; perigo pois não tem atomicidade
;(s/defn visualizacoes [db produto-id :- java.util.UUID]
;  (or (d/q '[:find ?visualizacoes .
;             :in $ ?produto-id
;             :where
;             [?produto :produto/id ?produto-id]
;             [?produto :produto/visualizacoes ?visualizacoes]]
;           db
;           produto-id)
;      0))
;
;(s/defn visualizacao! [conn produto-id :- java.util.UUID]
;  (let [ate-agora (visualizacoes (d/db conn) produto-id)
;        novo-valor (inc ate-agora)]
;    (d/transact conn [{:produto/id            produto-id
;                       :produto/visualizacoes novo-valor}])))

(s/defn visualizacao! [conn produto-id :- java.util.UUID]
  (d/transact conn [[:incrementa-visualizacao produto-id]]))