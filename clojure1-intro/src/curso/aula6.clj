(ns curso.aula6)

(def pedido {:mochila  {:quantidade 2
                        :preco      80}
             :camiseta {:quantidade 3
                        :preco      40}})
;
;(defn imprime-e-15 [valor]
;  (println "valor" (class valor) valor)
;  15)
;
;(println (map imprime-e-15 pedido))


; nao funciona!
;(defn imprime-e-15 [chave valor]
;  (println chave "e" valor)
;  15)
;
;(println (map imprime-e-15 pedido))

; destructuring: separa chave e valor em simbolos separados
(defn imprime-e-15 [[chave valor]]
  (println chave "e" valor)
  valor)

(println (map imprime-e-15 pedido))

; calcular 2 * 80 = 160
(defn preco-dos-produtos [[chave valor]]
  (* (:quantidade valor) (:preco valor)))

;como nao usei a chave, posso trocar por _
(defn preco-dos-produtos [[_ valor]]
  (* (:quantidade valor) (:preco valor)))

; preco total de cada item
(println (map preco-dos-produtos pedido))

; preco total do pedido
(println (reduce + (map preco-dos-produtos pedido)))

; THREAD LAST
(defn total-do-pedido
  [pedido]
  (->> pedido
      (map preco-dos-produtos ,,,)
      (reduce + ,,,)))

(println (total-do-pedido pedido))



; outra forma de destructuring

(defn preco-total-do-produto [produto]
  (* (:quantidade produto) (:preco produto)))

(defn total-do-pedido
  [pedido]
  (->> pedido
       vals
       (map preco-total-do-produto ,,,)
       (reduce + ,,,)))

(println (total-do-pedido pedido))


; usar filter com mapa
(def pedido {:mochila  {:quantidade 2
                        :preco      80}
             :camiseta {:quantidade 3
                        :preco      40}
             :chaveiro {:quantidade 1}})


(defn gratuito?
  [[chave item]]
  (<= (get item :preco 0) 0))

(println (filter gratuito? pedido))


; outra forma

(defn gratuito?
  [item]
  (<= (get item :preco 0) 0))

(println (filter (fn [[chave item]] (gratuito? item)) pedido))


(defn pago?
  [item]
  (not (gratuito? item)))


(println (pago? {:preco 50}))
(println (pago? {:preco 0}))
(println (pago? {}))


(def pago? (comp not gratuito?))


;O que aprendemos nesta aula:
;
;Utilizar o destruct para um dicionário;
;Utilizar o Thead last;
;Utilizar o map, reduce e filter em um mapa;
;Criar composição com o comp;
