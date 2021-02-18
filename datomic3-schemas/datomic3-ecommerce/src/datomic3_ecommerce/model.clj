(ns datomic3-ecommerce.model
  (:require [schema.core :as s]))

(defn uuid [] (java.util.UUID/randomUUID))

(def Categoria
  {:categoria/id   java.util.UUID
   :categoria/nome s/Str})

(def Produto
  {:produto/id                             java.util.UUID
   (s/optional-key :produto/nome)          s/Str
   (s/optional-key :produto/slug)          s/Str
   (s/optional-key :produto/preco)         BigDecimal
   (s/optional-key :produto/palavra-chave) [s/Str]
   (s/optional-key :produto/categoria)     Categoria
   (s/optional-key :produto/estoque)       s/Int
   (s/optional-key :produto/digital)       s/Bool})

; a desvantagem dessa abordagem é o copy-paste nas chaves
(s/defn novo-produto-alt
  [produto]
  (if (:produto/id produto)
    produto
    (assoc produto :produto/id (uuid))))

(s/defn novo-produto :- Produto
  ([nome slug preco]
   (novo-produto (uuid) nome slug preco))

  ([uuid nome slug preco]
   (novo-produto uuid nome slug preco 0))

  ; será que faz sentido aridade multipla?
  ; pois aí entramos no momento de polimorfismo e multiplos construtores
  ; poderíamos optar por um mapa.
  ([uuid nome slug preco estoque]
   {:produto/id      uuid
    :produto/nome    nome
    :produto/slug    slug
    :produto/preco   preco
    :produto/estoque estoque
    :produto/digital false}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid
    :categoria/nome nome}))