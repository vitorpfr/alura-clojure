(ns curso.aula5)

;(def estoque {"mochila" 10
;              "camiseta" 5})

(println estoque)

(println "Temos" (count estoque) "elementos")

(println "As chaves são:" (keys estoque))
(println "Os valores são:" (vals estoque))


; keyword
;:mochila

(def estoque {:mochila  10
              :camiseta 5})

(println (assoc estoque :cadeira 3))
(println estoque)

(println (assoc estoque :mochila 1))


(println estoque)
(println (update estoque :mochila inc))


; mapas aninhados
(def pedido {:mochila  {:quantidade 2
                        :preco      80}
             :camiseta {:quantidade 3
                        :preco      40}})

(println "\n\n\n\n\n")
(println pedido)

(def pedido (assoc pedido :chaveiro {:quantidade 1, :preco 10}))
(println pedido)

(println (pedido :mochila))                                 ;mapa como funcao
(println (get pedido :mochila))
(println (get pedido :cadeira {}))                          ;get com valor default
(println (:mochila pedido))
(println (:cadeira pedido))
(println (:cadeira pedido {}))



(println (update-in pedido [:mochila :quantidade] inc))     ; atualiza valor em mapa aninhado

; threading
(println (:quantidade (:mochila pedido)))

(println (-> pedido
             :mochila
             :quantidade))

(-> pedido
    :mochila
    :quantidade
    println)

;O que aprendemos nesta aula:
;
;Utilizar um Map(HashMap);
;Utilizar a função count;
;Utilizar a função keys para devolver as chaves que o map possui;
;Utilizar a função assoc para associar um valor ao map;
;O que é threading.
