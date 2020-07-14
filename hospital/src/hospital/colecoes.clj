(ns hospital.colecoes
  (:use [clojure pprint]))

(defn testa-vetor []
  (let [espera [111 222]]
    (println espera)

    ; colocar elemento
    (println "conj 333" (conj espera 333))
    (println "conj 444" (conj espera 444))

    ; retirar ultimo elemento - não é o ideal pra uma fila
    (println "pop" (pop espera))

    ))

(println "testando vetor")
(testa-vetor)

; na implementação de vetor, é mais fácil adicionar e remover
; do final, que é o que conj e pop fazem
; conclusão: não é viável usar vetor pra filas de hospital


(defn testa-lista []
  (let [espera '(111 222)]
    (println espera)

    ; colocar elemento
    (println "conj 333" (conj espera 333))
    (println "conj 444" (conj espera 444))

    ; retirar elemento
    (println "pop" (pop espera))

    ))

(println "testando lista")
(testa-lista)

; na implementação de lista, conj coloca no inicio e
; pop remove do inicio porque é mais facil também
; conclusão: também não é viável usar lista pra filas de hospital


(defn testa-conjunto []
  (let [espera #{111 222}]
    (println espera)

    ; colocar elemento
    (println "conj 333" (conj espera 333))
    (println "conj 444" (conj espera 444))

    ; retirar elemento
    ;(println "pop" (pop espera))

    ))

(println "testando conjunto")
(testa-conjunto)

; em conjunto (set), pop não funciona porque não existe uma
; ordem definida - não funciona pra uma fila de hospital


(defn testa-fila []
  (let [espera (conj clojure.lang.PersistentQueue/EMPTY "111" "222")]
    (println (seq espera))

    ; colocar elemento
    (println "conj 333" (seq (conj espera "333")))
    (println "conj 444" (seq (conj espera "444")))

    ; retirar elemento
    (println "pop" (seq (pop espera)))

    ; olhar quem é o primeiro da fila
    (println "peek" (peek espera))

    (pprint espera)

    ))

(println "testando fila")
(testa-fila)

;(clojure.lang.PersistentQueue/EMPTY)
