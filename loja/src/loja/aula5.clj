(ns loja.aula5
  (:require [loja.db :as l.db]
            [loja.logic :as l.logic]))


(defn gastou-bastante?
  [info-do-usuario]
  (> (:preco-total info-do-usuario) 500))

(let [pedidos (l.db/todos-os-pedidos)
      resumo (l.logic/resumo-por-usuario pedidos)]


  (println "filter" (filter gastou-bastante? resumo))
  ; filter retorna os elementos para o qual o predicado é true

  (println "keep" (keep gastou-bastante? resumo))
  ; keep aplica a função em cada elemento, e retorna os que ainda existem: map + filter


  )


; entendendo o que acontece por dentro do keep
(println "tentando entender dentro do filter e do keep")
(defn gastou-bastante?
  [info-do-usuario]
  (println "gastou-bastante?" (:usuario-id info-do-usuario))
  (> (:preco-total info-do-usuario) 500))

(let [pedidos (l.db/todos-os-pedidos)
      resumo (l.logic/resumo-por-usuario pedidos)]

  (println "keep" (keep gastou-bastante? resumo))
  )


(println "vamos isolar o funcionamento do keep")

(println (range 10))
(println (take 2 (range 100000000000000)))
; a sequencia não está sendo "gulosa" (eager) - ela não chega a
; gerar a seq inteira pra depois pegar os 2 primeiros
; está sendo lazy!

(defn filtro1 [x]
  (println "filtro1" x)
  x)

(println (map filtro1 (range 10)))

(defn filtro2 [x]
  (println "filtro2" x)
  x)

(println (map filtro2 (map filtro1 (range 10))))


(->> (range 10)
     (map filtro1)
     (map filtro2)
     println)



(->> (range 50)
     (map filtro1)
     (map filtro2)
     println)

;; map trabalha em chunks de 32 em 32 elementos - não é eager! (é semi-lazy)


;; se quisermos garantir que a coll só passa pelo filtro2 depois de passar pelo filtro1 completamente,
;; podemos usar mapv ao invés de map: ela retorna um vetor e garante que o vetor inteiro é retornado
(->> (range 50)
     (mapv filtro1)
     (mapv filtro2)
     println)


; lista ligada é 100% lazy nesse cenario - não tem os chunks implementados no vetor
(->> '(0 1 2 3 4 5 6 3 3 3 3 3 3 3 3 3 3 3 3 5)
     (map filtro1)
     (map filtro2)
     println)


;O que aprendemos nesta aula:
;
;Utilizar o keep;
;O que é comportamento EAGER e LAZY;
;O que são chunked;
;O que é uma lista ligada;
