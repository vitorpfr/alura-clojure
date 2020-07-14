(ns loja.aula1)
;["a", "b", "c"]

(def test-vector ["daniela", "guilherme", "carlos"])

(map println test-vector)                                   ;imprimir cada nome do vetor
(println (first test-vector))                               ;imprimir primeiro nome do vetor
(println (rest test-vector))                                ;imprimir todos nomes menos o primeiro
(println (next test-vector))                                ;imprimir todos nomes menos o primeiro

(println (seq []))
(println (seq test-vector))




(println "\n\n\n\nMEU MAPA")

; wrong implementation - enters in infinite loop
;(defn my-map
;  [function sequence]
;  (let [first-element (first sequence)]
;    (function first-element)
;    (my-map function (rest sequence))))

;(my-map println test-vector)
;
;(defn my-map
;  [function sequence]
;  (let [first-element (first sequence)]
;    (if first-element
;      (do (function first-element)
;          (my-map function (rest sequence))))))

;(my-map println test-vector)

; ["daniela", false, "carlos"] ;isso quebra a implemnetação anterior por causa do if

(defn my-map
  [function sequence]
  (let [first-element (first sequence)]
    (if (not (nil? first-element))
      (do (function first-element)
          (my-map function (rest sequence))))))

(my-map println test-vector)
(my-map println [])
(my-map println nil)

;(my-map println (range 100000))                             ; não funciona, stack overflow error, imprime só até 0 4883 por causa do numero de funcoes chamadas recursivamente, estourando o stack

; testando recur ao invés de chamar a própria função (recur é otimizado pelo compilador do clojure)
; TAIL RECURSION
(defn my-map-recur
  [function sequence]
  (let [first-element (first sequence)]
    (if (not (nil? first-element))
      (do (function first-element)
          (recur function (rest sequence))))))

(my-map-recur println (range 10000))                        ; funciona!

; recur precisa ser o retorno da função (tail) - esse exemplo abaixo não funciona!
;(defn my-wrong-map-recur
;  [function sequence]
;  (let [first-element (first sequence)]
;    (if (not (nil? first-element))
;      (do
;        (recur function (rest sequence))
;        (function first-element)))))


;Quando podemos usar o recur para efetuar a recursão na função?
;Só pode aparecer como retorno da função, isto é, na cauda.
;Exato. Se a função possui mais de um caminho (como no caso de um if), os dois caminhos podem apresentar um recur.

;O que aprendemos nesta aula:
;
;Como o map funciona;
;Utilizar a função first para pegar o primeiro elemento;
;Utilizar a função rest para pegar a partir do segundo elemento;
;Utilizar a função next para pegar o próximo elemento;
;Utilizar a função seq para ver a sequência de elementos;
;Utilizar a função do para rodar tudo que está dentro do if;
;Fazer recursão;
;Utilizar a função recur para dizer que estamos fazendo uma recursão.
