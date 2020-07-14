(ns loja.aula2)

;["daniela" "guilherme" "carlos" "paulo" "lucia" "ana"]

; errado, devemos usar recur ao inves de conta pra nao causar stack overflow
;(defn conta
;  [total-ate-agora elementos]
;  (conta (inc total-ate-agora) (rest elementos)))

; com recur - não funciona porque não tem o critério de parada
(defn conta
  [total-ate-agora elementos]
  (recur (inc total-ate-agora) (rest elementos)))

; RECURSÃO SEMPRE TEM QUE TER UM CRITÉRIO DE PARADA!
; retorna nil porque o else não foi implementado
(defn conta
  [total-ate-agora elementos]
  (if (next elementos)
    (recur (inc total-ate-agora) (next elementos))))

(defn conta
  [total-ate-agora elementos]
  (println total-ate-agora elementos)
  (if (seq elementos)
    (recur (inc total-ate-agora) (next elementos))
    total-ate-agora))

; funcao com mais de uma possibilidade de aridade
(defn minha-funcao
  ([parametro1] (println "p1" parametro1))
  ([parametro1 parametro2] (println "p2" parametro1 parametro2)))

(minha-funcao 1)


; implementando isso na nossa funcao de contar elementos, pra evitar passar o 0 como argumento
(defn conta

  ([elementos]
   (conta 0 elementos))

  ([total-ate-agora elementos]
   (if (seq elementos)
     (recur (inc total-ate-agora) (next elementos))
     total-ate-agora)))

(println (conta ["daniela" "guilherme" "carlos" "paulo" "lucia" "ana"]))


; outra variação da contagem de elementos usando loop
;input do loop:
;[elemento-que-vou-redefinir-1 valor-inicial-dele
; elemento-que-vou-redefinir-2 valor-inicial-dele]

;input do recur: os 2 elementos que eu disse que vou redefinir em cada iteração do loop

(defn outra-conta
  [elementos]
  (loop [total-ate-agora 0
         elementos-restantes elementos]
    (if (seq elementos-restantes)
      (recur (inc total-ate-agora) (next elementos-restantes))
      total-ate-agora)

    ))

(println (outra-conta ["daniela" "guilherme" "carlos" "paulo" "lucia" "ana"]))
(println (outra-conta []))


; Quando usar um loop e quando usar recursão?
; Daremos preferência a recursão ou loops isolados.
; Loop em geral parece ser um code smell uma vez que transparece controle de fluxo que poderia ter sido isolado em uma função.

;O que aprendemos nesta aula:
;
;Como o reduce funciona;
;Implementar o reduce;
;Variação de parâmetros na função;
;Utilizar o loop;
;Fazer recorrência dentro do loop.
