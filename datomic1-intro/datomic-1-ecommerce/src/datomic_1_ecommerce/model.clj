(ns datomic-1-ecommerce.model)

(defn novo-produto [nome slug preco]
  {:produto/nome nome
   :produto/slug slug
   :produto/preco preco})