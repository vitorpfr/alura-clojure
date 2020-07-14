(ns hospital.logic
  (:require [hospital.model :as h.model]
            [schema.core :as s]))

; Test Driven Development

; existe um problema de condicional quando o departamento não existe
; some-> : igual ao ->, mas se alguma função retorna nil, ela para e retorna nil
;(defn cabe-na-fila?
;  [hospital departamento]
;  (some-> hospital
;          departamento
;          count
;          (< 5)))

; funciona para o caso de não ter o departamento
;(defn cabe-na-fila?
;  [hospital departamento]
;  (when-let [fila (get hospital departamento)]
;    (-> fila
;        count
;        (< 5))))

; também funciona mas usa o some->
; some-> : igual ao ->, mas se alguma função retorna nil, ela para e retorna nil
; desvantagem: qq um que der nil, devolve nil
(defn cabe-na-fila?
  [hospital departamento]
  (some-> hospital
          departamento
          count
          (< 5)))



;(defn chega-em
;  [hospital departamento pessoa]
;  (if (cabe-na-fila? hospital departamento)
;    (update hospital departamento conj pessoa))
;  )

(defn- chega-em
  [hospital departamento pessoa]
  (if (cabe-na-fila? hospital departamento)
    (update hospital departamento conj pessoa)
    (throw (ex-info "Não cabe ninguém neste departamento" {:paciente pessoa :tipo :impossivel-colocar-pessoa-na-fila})))
  )

(defn- tenta-colocar-na-fila
  [hospital departamento pessoa]
  (if (cabe-na-fila? hospital departamento)
    (update hospital departamento conj pessoa)))

;(defn chega-em
;  [hospital departamento pessoa]
;  (if-let [novo-hospital (tenta-colocar-na-fila hospital departamento pessoa)]
;    {:hospital novo-hospital :resultado :sucesso}
;    {:hospital hospital :resultado :impossivel-colocar-pessoa-na-fila}))

;(println (chega-em {:espera [1 2 3 5 4]} :espera 6))


; problema dessa abordagem: antes de fazer swap chega-em, vai ter que tratar o resultado (porque agora
; devolve um mapa ao inves do hospital


; código de um curso anterior

(defn chega-em
  [hospital departamento pessoa]
  (if (cabe-na-fila? hospital departamento)
    (update hospital departamento conj pessoa)
    (throw (ex-info "Não cabe ninguém neste departamento" {:paciente pessoa}))))

(s/defn atende :- h.model/Hospital
  [hospital :- h.model/Hospital, departamento :- s/Keyword]
  (update hospital departamento pop))

(s/defn proxima :- h.model/Paciente
  "Retorna o próximo paciente da fila"
  [hospital :- h.model/Hospital, departamento :- s/Keyword]
  (-> hospital
      departamento
      peek))

(s/defn transfere :- h.model/Hospital
  "Transfere o próximo paciente da fila de para a fila para"
  [hospital :- h.model/Hospital, de :- s/Keyword, para :- s/Keyword]
  ; em clojure muitas vezes essa parte voltada a contratos não é usada
  ; é favorecido ifs, schemas, testes, etc
  {:pre  [(contains? hospital de)
          (contains? hospital para)]                        ; pre condição da função
   :post [(= (+ (count (get hospital para)) (count (get hospital de))) (+ (count (get % para)) (count (get % de))))]} ; pos-condicao da funcao: % é o output da funcao
  (let [pessoa (proxima hospital de)]
    (-> hospital
        (atende de)
        (chega-em para pessoa))))

; :post está testando que tem a mesma quantidade de pessoas antes e depois da transferencia

;
