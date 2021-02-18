(ns datomic3-ecommerce.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [datomic3-ecommerce.db :as db]
            [datomic3-ecommerce.model :as model]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(db/cria-dados-de-exemplo! conn)

(def categorias (db/todas-as-categorias (d/db conn)))
(pprint categorias)
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

(def dama {:produto/nome "Dama"
           :produto/slug "/dama"
           :produto/preco 15.5M
           :produto/id (model/uuid)})

(db/adiciona-ou-altera-produtos! conn [dama])
(db/um-produto (d/db conn) (:produto/id dama))

; update/insert ==> upsert
(db/adiciona-ou-altera-produtos! conn [(assoc dama :produto/slug "/jogo-de-dama")])
(db/um-produto (d/db conn) (:produto/id dama))

; como dama é imutavel, aqui o slug volta a ser /dama ao inves de /jogo-de-dama
(db/adiciona-ou-altera-produtos! conn [(assoc dama :produto/preco 150.5M)])
(db/um-produto (d/db conn) (:produto/id dama))

; detectamos um problema que é uma dificuldade... entender que updates podem sobrescrever campos anteriores
; ex: se duas pessoas fizerem um update simultaneo da dama, como acima

(defn atualiza-preco []
  (println "atualizando preco")
  (let [produto {:produto/id (:produto/id dama) :produto/preco 111M}]
    (db/adiciona-ou-altera-produtos! conn [produto])
    (println "Preco atualizado")
    produto))

(defn atualiza-slug []
  (println "Atualizando slug")
  (let [produto {:produto/id (:produto/id dama) :produto/slug "/dama-com-slug-novo"}]
    (Thread/sleep 3000)
    (db/adiciona-ou-altera-produtos! conn [produto])
    (println "Slug atualizado")
    produto))

(defn roda-transacoes [tx]
  (let [futuros (mapv #(future (%)) tx)]
    (pprint (map deref futuros))
    (pprint "Resultado final")
    (pprint (db/um-produto (d/db conn) (:produto/id dama)))))

(roda-transacoes [atualiza-preco atualiza-slug])