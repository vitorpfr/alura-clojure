(ns datomic-1-ecommerce.aula2
  (:use clojure.pprint)
  (:require [datomic-1-ecommerce.db :as db]
            [datomic-1-ecommerce.model :as m]
            [datomic.api :as d]))

(db/apaga-banco)

(def conn (db/abre-conexao))

(db/cria-schema conn)

; adicionar um produto novo que só tem nome - ele deixou transacionar no db!
; o datomic suporta só um dos identificadores
(let [calculadora {:produto/nome "Calculadora com 4 operações"}]
  (d/transact conn [calculadora]))


; consigo colocar um valor nulo? - nesse caso ele não deixa
; não funciona pois se você quer algo "vazio", é só não colocar
(let [radio-relogio {:produto/nome "Radio com relogio"
                     :produto/slug nil}]
  (d/transact conn [radio-relogio]))

; atualizando o preço (antes 8888.10, agora 0.1)
; nesse caso ele adiciona dois datoms: o valor antigo como false, e o valor novo como true)
(let [celular-barato (m/novo-produto "Celular Barato" "/celular-barato" 8888.10M)
      resultado @(d/transact conn [celular-barato])
      id-entidade-inserida (-> resultado :tempids vals first)]
  @(d/transact conn [[:db/add id-entidade-inserida :produto/preco 0.1M]]))

; resultado:
;{:db-before datomic.db.Db,
; @a6161e4 :db-after,
; datomic.db.Db @911ea78d,
; :tx-data [#datom[13194139534315 50 #inst"2021-02-08T21:39:30.405-00:00" 13194139534315 true]
;           #datom[17592186045418 74 0.1M 13194139534315 true]
;           #datom[17592186045418 74 8888.10M 13194139534315 false]],
; :tempids {}}

; e se eu quiser só tirar o slug?
(let [celular-barato (m/novo-produto "Celular Barato" "/celular-barato" 8888.10M)
      resultado @(d/transact conn [celular-barato])
      id-entidade-inserida (-> resultado :tempids vals first)]
  (pprint @(d/transact conn [[:db/add id-entidade-inserida :produto/preco 0.1M]]))
  (pprint @(d/transact conn [[:db/retract id-entidade-inserida :produto/slug "/celular-barato"]])))
; se eu especificar o nome errado no db/retract pra remover, ele não faz nada e só registra a transação
