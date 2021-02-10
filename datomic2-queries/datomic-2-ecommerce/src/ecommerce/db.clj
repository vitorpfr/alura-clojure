(ns ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]))

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

(defn cria-schema! [conn]
  (d/transact conn schema))

; pull explicito atributo a atributo
;(defn todos-os-produtos [db]
;  (d/q '[:find (pull ?entidade [:produto/nome :produto/preco :produto/slug])
;         :where [?entidade :produto/nome]] db))

; pull generico, vantagem preguica, desvantagem pode trazer mais do que eu queira
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

; no sql eh comum fazer:
; String sql = "meu codigo sql";
; conexao.query(sql)

; esse aqui eh similar ao String sql
; eh comum e voce pode querer extrair em um def ou let
; porem... -q é notacao hungara... indica o TIPO... humm.. não parece ser legal
; em clojure
; vc vai encontrar esse padro em alguns exemplos e documentacao
; nao recomendamos notacao hungara dessa maneira, ainda menos abreviada ;)
(def todos-os-produtos-por-slug-fixo-q
  '[:find ?entidade
    :where [?entidade :produto/slug "/computador-novo"]])

(defn todos-os-produtos-por-slug-fixo [db]
  (d/q todos-os-produtos-por-slug-fixo-q db))

; não estou usando notacao hungara e extract
; eh comum no sql: String sql = "select * from where slug=::SLUG::"
; conexao.query(sql, {::SLUG:: "/computador-novo})
(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-procurado                              ; proposital diferente da variável de clojure para evitar erros
         :where [?entidade :produto/slug ?slug-procurado]]
       db slug))


; ?entity => ?entidade => ?produto => ?p
; se não vai usar... _
(defn todos-os-slugs [db]
  (d/q '[:find ?slug
         :where [_ :produto/slug ?slug]] db))

; estou sendo explicito nos campos 1 a 1
(defn todos-os-produtos-por-preco [db preco-minimo-requisitado]
  (d/q '[:find ?nome, ?preco
         :in $, ?preco-minimo
         :keys produto/nome, produto/preco
         :where [?produto :produto/preco ?preco]
         [(> ?preco ?preco-minimo)]
         [?produto :produto/nome ?nome]]
       db, preco-minimo-requisitado))


; eu tenho 10mil...se eu tenho 1000 produtos com preco > 5000, so 10 produtos com quantidade < 10
; passar por 10 mil
;[(> preco  5000)]                                           ; => 5000 datom
;[(< quantidade 10)]                                         ; => 10 datom
;
;; passar por 10 mil
;[(< quantidade 10)]                                         ; => 10 datom
;[(> preco  5000)]                                           ; => 10 datom
;
; em geral vamos deixar as condicoes da mais restritiva pra menos restritiva...
; pois o plano de ação somos nós quem tomamos

(defn todos-os-produtos-por-palavra-chave [db palavra-chave-buscada]
  (d/q '[:find (pull ?produto [*])
         :in $ ?palavra-chave
         :where [?produto :produto/palavra-chave ?palavra-chave]]
       db palavra-chave-buscada))

; pegar só um produto por dbid
(defn um-produto-por-dbid [db db-id]
  (d/pull db '[*] db-id))

; pegar só um produto por product id
; por padrão o pull usa o identificador do banco,
; temos que informar um localizador [:produto/id produto-id]
; o que quer dizer: quem tem esse id é o que to tentando buscar
(defn um-produto [db produto-id]
  (d/pull db '[*] [:produto/id produto-id]))

; ex: se eu quiser, através do id, trazer a data de nascimento e cidade de um usuario
(comment
  (d/pull db '[:usuario/data-de-nascimento :usuario/cidade] [:usuario/id ?usuario_id]))


(defn todas-as-categorias [db]
  (d/q '[:find (pull ?categoria [*])
         :where [?categoria :categoria/id]] db))

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

; datomic.tx se refere ao id da transação atual
(defn adiciona-produtos!
  ([conn produtos] (d/transact conn produtos))
  ([conn produtos ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

; como esses dois estão genéricos poderiam ser um só
; vamos manter 2 pra poder usar schema
(defn adiciona-categorias! [conn categorias]
  (d/transact conn categorias))

; essa query abaixo é como um "join" feito no datomic!
(defn todos-os-nomes-de-produtos-e-categorias [db]
  (d/q '[:find ?prod-nome ?cat-nome
         :keys produto categoria
         :where [?produto :produto/nome ?prod-nome]
         [?produto :produto/categoria ?cat]
         [?cat :categoria/nome ?cat-nome]]
       db))

;;; exemplo com forward navigation
; quem eu estou referenciando?
; ?produto :produto/categoria >>>>
; dentro do pull eu coloco o que eu quero que venha dos produtos
; mas se eu colocar :produto/categoria, vai vir o numero da categoria
; como faço pra que venha o nome da categoria? forward! (ver documentação do pull)
; se eu quisesse trazer tudo da categoria, posso colocar {:produto/categoria [*]} dentro do pull
(defn todos-os-produtos-da-categoria-old [db nome-da-categoria]
  (d/q '[:find (pull ?produto [:produto/nome :produto/slug {:produto/categoria [:categoria/nome]}])
         :in $ ?categoria-buscada
         :where
         [?cat :categoria/nome ?categoria-buscada]
         [?produto :produto/categoria ?cat]]
       db nome-da-categoria))

; a navegação da query foi: categoria -> produto -> pull forward do nome da categoria
; se eu tenho a categoria já, posso fazer um pull backward direto
; (quem tem esse nome de categoria?)


;;; exemplo com backward navigation
; quem está me referenciando?
; >>>>>>> :produto/_categoria ?categoria
; a forma de sinalizar que é um backward navigation é com o _
(defn todos-os-produtos-da-categoria [db nome-da-categoria]
  (d/q '[:find (pull ?cat [:categoria/nome {:produto/_categoria [:produto/nome :produto/slug]}])
         :in $ ?categoria-buscada
         :where
         [?cat :categoria/nome ?categoria-buscada]]
       db nome-da-categoria))


; forward navigation e backward navigation são formas de fazer o join entre entidades
; no forward navigation eu começo pelos produtos e procuro quem eles estão referenciando (categoria)
; no backward navigation eu começo pela categoria e procuro quem a referencia

; nunca fazer count de preco pq o retorno da agregação é um set, deduplica elementos
; ou seja, se dois produtos tiverem o mesmo preço, ele não contaria os dois!
; pra corrigir, a gente usa o :with com a entidade relacionada
(defn resumo-dos-produtos [db]
  (d/q '[:find (min ?preco) (max ?preco) (count ?preco)
         :keys minimo maximo quantidade
         :with ?produto
         :where [?produto :produto/preco ?preco]]
       db))

(defn resumo-dos-produtos-por-categoria [db]
  (d/q '[:find ?nome (min ?preco) (max ?preco) (count ?preco) (sum ?preco)
         :keys categoria minimo maximo quantidade preco-total
         :with ?produto
         :where
         [?produto :produto/preco ?preco]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome]]
       db))

; resumo de group by/agg:
; entidade sendo agrupada tem que estar no :with
; valor do grupo by tem que ser o primeiro do find
; aggs vem depois

; variação que faz duas queries soltas
(defn todos-os-produtos-mais-caros-old [db]
  (let [maior-preco (ffirst (d/q '[:find (max ?preco)
                                   :where [_ :produto/preco ?preco]]
                                 db))]
    (d/q '[:find (pull ?produto [*])
           :in $ ?preco
           :where [?produto :produto/preco ?preco]]
         db maior-preco)))

; problema dessa abordagem: estou fazendo duas queries!
; queremos fazer as duas queries de uma vez só
(defn todos-os-produtos-mais-caros [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (max ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]]
         [?produto :produto/preco ?preco]] db))
; a query começa pelo where
; na primeira clausula do where, ele executa a query e coloca o resultado em ?preco
; na segunda, ele executa com o preço definido

(defn todos-os-produtos-mais-baratos [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (min ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]]
         [?produto :produto/preco ?preco]] db))

; transacoes tambem sao entidades
; como entidades, transacoes tambem podem ter atributos adicionados (ex: usuario que fez a transacao)

; nesse caso, eu pego o ip buscado, pego as transacoes feitas por esse ip e dps filtro os produtos adicionados
; nessas transacoes
(defn todos-os-produtos-do-ip [db ip]
  (d/q '[:find (pull ?produto [*])
         :in $ ?searched-ip
         :where
         [?tx :tx-data/ip ?searched-ip]
         [?produto :produto/id _ ?tx]] db ip))