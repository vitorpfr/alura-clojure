(ns curso.aula4)

(def precos [30 700 1000])

(println (precos 0))
(println (get precos 0))
(println (get precos 2))
(println (get precos 17))
(println (get precos 17 0))                                 ; indice 17, se nao existir retorna valor 0


;(println (conj precos 5))


(println (+ 5 1))
(println (inc 5))

(update precos 0 inc)

(defn soma-3
  [valor]
  (println "estou somando 3 em" valor)
  (+ valor 3))

(println (update precos 0 soma-3))



; CODIGO DA AULA ANTERIOR
(defn aplica-desconto?
  [valor-bruto]
  (> valor-bruto 100))

(defn valor-descontado
  "Retorna o valor com desconto de 10% se o valor bruto for estritamente maior que 100."
  [valor-bruto]
  (if (aplica-desconto? valor-bruto)
    (let [taxa-de-desconto (/ 10 100)
          desconto (* valor-bruto taxa-de-desconto)]
      (- valor-bruto desconto))
    valor-bruto))


; map

(println "map" (map valor-descontado precos))

(println (range 10))
(println (filter even? (range 10)))

(println "vetor" precos)
(println "filter" (filter aplica-desconto? precos))

(println "map apos o filter" (map valor-descontado (filter aplica-desconto? precos)))


; reduce

(reduce + precos)


(defn minha-soma
  [valor-1 valor-2]
  (println "somando" valor-1 "e" valor-2)
  (+ valor-1 valor-2))

(reduce minha-soma precos)
(reduce minha-soma 0 precos)
(reduce minha-soma (range 10))

;O que aprendemos nesta aula:
;
;Utilizar o get para evitar exceções;
;Definir o valor padrão de retorno do get;
;Utilizar a função inc para somar o número atual mais um;
;Utilizar a função update para apenas retornar um vetor com um valor alterado;
;Utilizar a função map para passar por todos os elementos;
;Utilizar a função filter para fazer a filtragem de elementos;
;Utilizar a função reduce para reduzir valores.
