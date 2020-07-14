(ns hospital.logic
  (:use [clojure pprint]))

(defn cabe-na-fila?
  [hospital departamento]
  (-> hospital
      (get,,, departamento)
      count
      (<,,, 5)))

(defn chega-em
  [hospital departamento pessoa]
  (if (cabe-na-fila? hospital departamento)
    (update hospital departamento conj pessoa)
    (throw (ex-info "Fila já está cheia" {:tentando-adicionar pessoa})))

  )

(defn chega-em-pausado
  [hospital departamento pessoa]
  (if (cabe-na-fila? hospital departamento)
    (do (Thread/sleep 1000)
        (update hospital departamento conj pessoa))
    (throw (ex-info "Fila já está cheia" {:tentando-adicionar pessoa})))
  )

(defn chega-em-pausado-logando
  [hospital departamento pessoa]
  (println "tentando adicionar a pessoa" pessoa)
  (Thread/sleep (* (rand) 2000))
  (if (cabe-na-fila? hospital departamento)
    (do
      (println "dei o update na pessoa" pessoa)
      (update hospital departamento conj pessoa))
    (throw (ex-info "Fila já está cheia" {:tentando-adicionar pessoa})))
  )

; atender pessoa em uma fila
(defn atende
  "Atende (remove) a pessoa de uma fila"
  [hospital departamento]
  (update hospital departamento pop))

(defn proxima-pessoa-da-fila
  "Retorna o próximo paciente da fila"
  [hospital departamento]
  (-> hospital
      departamento
      peek))

(defn transfere
  "Transfere a proxima pessoa da fila de para a fila para"
  [hospital de para]
  (let [pessoa (proxima-pessoa-da-fila hospital de)]
    (-> hospital
        (atende de)
        (chega-em para pessoa)))

  )



; atender pessoa em uma fila - retorna também quem foi atendido
(defn atende-completo
  "Atende (remove) a pessoa de uma fila"
  [hospital departamento]
  {:paciente (update hospital departamento peek)
   :hospital (update hospital departamento pop)})


; atender pessoa em uma fila - retorna também quem foi atendido
(defn atende-completo-que-chama-ambos
  "Atende (remove) a pessoa de uma fila"
  [hospital departamento]
  (let [fila (get hospital departamento)
        peek-pop (juxt peek pop)
        [pessoa fila-atualizada] (peek-pop fila)
        hospital-atualizado (update hospital assoc departamento fila-atualizada)]
    {:paciente pessoa
     :hospital hospital-atualizado}
    )

  )
