(ns loja.aula3
  (:require [loja.db :as l.db]))

(println (l.db/todos-os-pedidos))

; agrupar por usuario
;(println (group-by :usuario (l.db/todos-os-pedidos)))

; definir funcao de agrupamento customizada pra ver o que está acontecendo
(defn minha-funcao-de-agrupamento
  [elemento]
  (println "elemento" elemento)
  (:usuario elemento))

; mostra os pedidos agrupados por usuario
(println (group-by minha-funcao-de-agrupamento (l.db/todos-os-pedidos)))

; tentando contar o total de pedidos - 4, que na verdade é o total de usuarios
(println (count (vals (group-by :usuario (l.db/todos-os-pedidos)))))

; correção pra contar o numero de pedidos, e não o de usuarios
(println (map count (vals (group-by :usuario (l.db/todos-os-pedidos)))))

; transformar em thread pra facilitar a leitura
(->> (l.db/todos-os-pedidos)
     (group-by :usuario)
     vals
     (map count)
     println)
; problema: o vals some com o id dos usuarios, então não sabemos de quem é cada numero de pedidos

; solução: substituir por fn que conta o total por usuario
(println "nova implementação")

(defn conta-total-por-usuario
  [[usuario pedidos]]
  [usuario (count pedidos)]
  )

(->> (l.db/todos-os-pedidos)
     (group-by :usuario)
     (map conta-total-por-usuario)
     println)
; problema: vetor não é prático pra ler as infos de saída
; solução: devolver um mapa
(println "nova implementação com mapa")

(defn conta-total-por-usuario
  [[usuario pedidos]]
  {:usuario-id usuario
   :total-de-pedidos (count pedidos)}
  )

(->> (l.db/todos-os-pedidos)
     (group-by :usuario)
     (map conta-total-por-usuario)
     println)

; incluir também o valor total dos pedidos daquele usuario
(println "nova implementação com total dos pedidos")

(defn total-do-item
  [[_ detalhes]]
  (* (get detalhes :quantidade 0) (get detalhes :preco-unitario 0)))

(defn total-do-pedido
  [pedido]
  (->> pedido
       (map total-do-item)
       (reduce +)))

(defn total-dos-pedidos
  [pedidos]
  (->> pedidos
       (map :itens)
       (map total-do-pedido)
       (reduce +)))


(defn quantia-de-pedidos-e-gasto-total-por-usuario
  [[usuario pedidos]]
  {:usuario-id usuario
   :total-de-pedidos (count pedidos)
   :preco-total (total-dos-pedidos pedidos)}
  )

(->> (l.db/todos-os-pedidos)
     (group-by :usuario)
     (map quantia-de-pedidos-e-gasto-total-por-usuario)
     println)


;O que aprendemos nesta aula:
;
;Simular um banco em memória;
;Utilizar o require para fazer a importação de classe;
;Utilizar um as para abreviação;
;Agrupar dados.
