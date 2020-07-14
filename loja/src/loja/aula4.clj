(ns loja.aula4
  (:require [loja.db :as l.db]
            [loja.logic :as l.logic]))

;compras agrupadas por usuario
(let [pedidos (l.db/todos-os-pedidos)
      resumo (l.logic/resumo-por-usuario pedidos)]
  (println "Resumo" resumo)
  (println "Resumo ordenado" (sort-by :preco-total resumo))
  (println "Resumo ordenado ao contrario" (reverse (sort-by :preco-total resumo)))
  (println "Resumo ordenado por id" (sort-by :usuario-id resumo))

  (println "Quantidade de mochilas do primeiro pedido:" (get-in pedidos [0 :itens :mochila :quantidade]))
  )


(defn resumo-por-usuario-ordenado
  [pedidos]
  (->> pedidos
       l.logic/resumo-por-usuario
       (sort-by :preco-total)
       reverse))

(println "após implementar a função")
(let [pedidos (l.db/todos-os-pedidos)
      resumo (resumo-por-usuario-ordenado pedidos)]
  (println "Resumo" resumo)
  (println "Primeiro" (first resumo))
  (println "Resto (todos menos o primeiro" (rest resumo))
  (println "Total" (count resumo))
  (println "Classe" (class resumo))
  (println "nth 1" (nth resumo 1))
  (println "take 2 primeiros elementos" (take 2 resumo))

  )

(defn top-2 [resumo]
  (take 2 resumo))


(let [pedidos (l.db/todos-os-pedidos)
      resumo (resumo-por-usuario-ordenado pedidos)]
  (println "Resumo" resumo)
  (println "Top-2" (top-2 resumo))

  )

; filter: como filtrar pessoas que gastaram mais de x reais?

(let [pedidos (l.db/todos-os-pedidos)
      resumo (resumo-por-usuario-ordenado pedidos)]
  ;(println (filter (fn [n] (> (:preco-total n) 500)) resumo))
  (println ">500 filter" (filter #(> (:preco-total %) 500) resumo))
  (println ">500 filter empty not?" (not (empty? (filter #(> (:preco-total %) 500) resumo))))
  (println "some: alguem gastou mais de 500?" (some #(> (:preco-total %) 500) resumo))
  (println "some: alguem gastou mais de 2000?" (some #(> (:preco-total %) 2000) resumo))
  )

;O que aprendemos nesta aula:
;
;Ordenar elementos com sort-by;
;Utilizar o reverse;
;Utilizar o get para pegar elementos por índice;
;Utilizar o nth para pegar o enésimo elemento;
;Utilizar o take para os dois primeiros elementos;
;Utilizar o some para verificar alguma condição dentro de uma coleção.
