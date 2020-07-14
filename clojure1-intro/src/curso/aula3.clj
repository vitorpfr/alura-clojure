(ns curso.aula3)

; PREDICATE - funções que retornam true ou false
;(defn aplica-desconto?
;  [valor-bruto]
;  (if (> valor-bruto 100)
;    true
;    false))
;
;(defn aplica-desconto?
;  [valor-bruto]
;  (when (> valor-bruto 100)
;    true))

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

(println (valor-descontado 1000))
(println (valor-descontado 100))


; funcao como parametro

(defn mais-caro-que-100?
  [valor-bruto]
  (> valor-bruto 100))

(defn valor-descontado
  "Retorna o valor com desconto de 10% se deve aplicar o desconto."
  [aplica? valor-bruto]
  (if (aplica? valor-bruto)
    (let [taxa-de-desconto (/ 10 100)
          desconto (* valor-bruto taxa-de-desconto)]
      (- valor-bruto desconto))
    valor-bruto))

(println "função como parametro")
(println (valor-descontado mais-caro-que-100? 1000))
(println (valor-descontado mais-caro-que-100? 100))


; HIGHER ORDER FUNCTIONS
; IMUTABILITY

(println "função sem nome ou anonima")

(println (valor-descontado (fn [valor-bruto] (> valor-bruto 100)) 1000))
(println (valor-descontado (fn [valor-bruto] (> valor-bruto 100)) 100))

(println (valor-descontado (fn [v] (> v 100)) 1000))
(println (valor-descontado #(> %1 100) 100))
(println (valor-descontado #(> % 100) 100))


;Lembre-se que atalhos fundamentais serão o de reload: Command Shift L e de execução da linha base em que você está trabalhando Command Shift P.

;O que aprendemos nesta aula:
;
;Utilizar o plugin Cursive;
;O que é o namespace;
;Atalhos do Intellij;
;Utilizar o ; para comentar a linha;
;O que são predicates;
;Fazer uma função chamar a outra;
;Criar uma função anônima;
;Utilizar % para fazer um função lambda.
