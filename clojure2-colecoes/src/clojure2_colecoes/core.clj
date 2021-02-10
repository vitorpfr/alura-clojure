(ns clojure2-colecoes.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

; my map
(defn my-map [fn coll]
  (loop [acc []
         [head & tail] coll]
    (if (empty? tail)
      (conj acc (fn head))
      (recur (conj acc (fn head)) tail))))

(my-map inc [1 2 3])

; aula

;["daniela" "guilherme" "carlos" "paulo"]
;{"guilherme" 37, "paulo" 39}

(map println ["daniela" "guilherme" "carlos" "paulo"])

(println "meu mapa")

(defn my-other-map
  [fn coll]
  (let [prim (first coll)]
    (fn prim)
    (my-other-map fn (rest coll))))

;(my-other-map println ["daniela" "guilherme" "carlos" "paulo"])

(defn my-other-map
  [fn coll]
  (let [prim (first coll)]
    (if prim
      (do (fn prim)
          (my-other-map fn (rest coll))))))

(my-other-map println ["daniela" "guilherme" "carlos" "paulo"])
; não funciona pq nao imprimiria o valor "false"

; essa funciona, através de recursão
(defn my-other-map
  [fn coll]
  (let [prim (first coll)]
    (if (not (nil? prim))
      (do (fn prim)
          (my-other-map fn (rest coll))))))

(my-other-map println ["daniela" "guilherme" "carlos" "paulo"])

; problema de recursão é que as execucoes vao empilhando no stack, e isso vai estourar a memoria (vai causar stack overflow)
;(my-other-map println (range 10000)) ; esse vai dar stackoverflowerror

; solucao é usar recur
(defn my-other-map
  [fn coll]
  (let [prim (first coll)]
    (if (not (nil? prim))
      (do (fn prim)
          (recur fn (rest coll))))))

(my-other-map println (range 10000))                        ;  esse funciona

(defn conta
  [total-ate-agora elementos]
  (if (next elementos)
    (recur (inc total-ate-agora) (next elementos))
    (inc total-ate-agora)))

(conta 0 ["daniela" "guilherme" "carlos" "paulo"])
; nao funciona com vetor vazio

(defn conta
  [total-ate-agora elementos]
  (if (seq elementos)
    (recur (inc total-ate-agora) (next elementos))
    total-ate-agora))

(conta 0 ["daniela" "guilherme" "carlos" "paulo"])
(conta 0 [])

(defn conta-com-loop
  [elementos]
  (loop [total-ate-agora 0
         elementos-restantes elementos]
    (if (seq elementos-restantes)
      (recur (inc total-ate-agora) (next elementos-restantes))
      total-ate-agora)))

(conta-com-loop ["daniela" "guilherme" "carlos" "paulo"])
(conta-com-loop [])