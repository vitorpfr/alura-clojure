(ns ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco []
  (d/delete-database db-uri))

; Produtos
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco pontoflutuante 1 ==> 3500.10

; Como pensar em datomic:
; A entidade é o produto
; id_entidade atributo valor
; 15 :produto/nome Computador Novo    ID_TX       operacao
; 15 :produto/slug /computador_novo     ID_TX       operacao
; 15 :produto/preco 3500.10    ID_TX       operacao
; 17 :produto/nome Telefone Caro    ID_TX       operacao
; 17 :produto/slug /telefone    ID_TX       operacao
; 17 :produto/preco 8888.88    ID_TX       operacao

; schema da entity "produto"
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
              :db/doc         "O preço de um produto com precisão monetária"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}])


(defn cria-schema [conn]
  (d/transact conn schema))

; pull explicito atributo a atributo
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [:produto/nome :produto/preco :produto/slug])
         :where [?entidade :produto/nome]] db))

; pull generico - pode trazer mais do que eu queria
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

; no sql é comum fazer:
; String sql = "meu codigo sql"
; conexao.query(sql)

; esse aqui é similar ao string sql
; é comum extrair a query
; porem -q é notacao hungara.. indica o tipo.. não parece ser legal em clojure
; vc vai encontrar esse padrão em alguns exemplos, mas não recomendamos, ainda menos abreviada
(def todos-os-produtos-por-slug-fixo-q
  '[:find ?entidade
    :where [?entidade :produto/slug "/computador-novo"]])

(defn todos-os-produtos-por-slug-fixo [db]
  (d/q todos-os-produtos-por-slug-fixo-q db))

; não estou usando notação hungara e o def
; é comum no sql:
; String sql = "select * from where slug=::SLUG::"
; conexao.query(sql, {::SLUG:: "/computador-novo}
(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-a-ser-buscado                          ; dois argumentos; o banco e slug
         :where [?entidade :produto/slug ?slug-a-ser-buscado]] db slug)) ; dois argumentos; o banco e slug

; ?entity => ?entidade => ?produto => ?p
; se não vai usar ?p, pode ser _
(defn todos-os-slugs [db]
  (d/q '[:find ?slug
         :where [_ :produto/slug ?slug]] db))

; estou sendo explícito nos campos um a um
(defn todos-os-produtos-por-preco [db preco-minimo-requisitado]
  (d/q '[:find ?nome ?preco
         :in $ ?preco-minimo
         :keys produto/nome produto/preco                   ; transforma o retorno de vetor pra mapa
         :where [?produto :produto/preco ?preco]
                [(> ?preco ?preco-minimo)]                         ; melhor filtrar o preço antes de pegar o nome
                [?produto :produto/nome ?nome]

         ] db preco-minimo-requisitado))

; se eu tenho 1000 produtos com preco > 5000, so 10 produtos com quantidade < 10:
;(> preco 5000)                                              ; => 1000 datoms
;(< quantidade 10)                                           ; => 10 datoms

; solução ótima: filtrar primeiro o caso de 10
;(< quantidade 10)                                           ; => 10 datoms
;(> preco 5000)                                              ; => 10 datoms

; boa prática: colocar primeiro a condição mais restritiva
; em geral, vamos deixar as condicoes da mais restritiva para a menos restritiva, pois
; o plano de ação somos nós que definimos

; forma antiga
;(defn todos-os-produtos-por-palavra-chave [db palavra-chave-buscada]
;  (d/q '[:find (pull ?produto [*])
;         :in $ ?palavra-chave-requisitada
;         :where [?produto :produto/palavra-chave ?palavra-chave]
;                [(= ?palavra-chave ?palavra-chave-requisitada)]
;         ] db palavra-chave-buscada))

; forma nova
(defn todos-os-produtos-por-palavra-chave [db palavra-chave-buscada]
  (d/q '[:find (pull ?produto [*])
         :in $ ?palavra-chave
         :where [?produto :produto/palavra-chave ?palavra-chave]
         ] db palavra-chave-buscada))
