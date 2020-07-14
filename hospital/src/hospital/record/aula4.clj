(ns hospital.record.aula4
  (:use clojure.pprint))

; create a record (like a class, but implemented as a map)
(defrecord PacienteParticular [id nome nascimento situacao])
(defrecord PacientePlanoDeSaude [id nome nascimento situacao plano])

; example of an object created
(pprint (->PacienteParticular 15 "Vitor" "01/04/1991" :normal))

; deve assinar pre-autorizacao
; particular: valor >= 50
; plano de saude: procedimento está no plano

(defprotocol Cobravel
  (deve-assinar-pre-autorizacao? [paciente procedimento valor]))

(defn nao-eh-urgente? [paciente]
  (not= :urgente (:situacao paciente :normal)))

(extend-type PacienteParticular
  Cobravel
  (deve-assinar-pre-autorizacao? [paciente procedimento valor]
    (and (nao-eh-urgente? paciente) (>= valor 50))))

(extend-type PacientePlanoDeSaude
  Cobravel
  (deve-assinar-pre-autorizacao? [paciente procedimento valor]
    (let [plano (:plano paciente)]
      (and (nao-eh-urgente? paciente) (not (some #(= % procedimento) plano))))))


; testando
(let [particular (PacienteParticular. 54 "Vitor" "01/04/1991" :urgente)
      plano (PacientePlanoDeSaude. 55 "Vitor" "04/01/1992" :urgente [:raio-x :ultrassom])]
  (pprint (deve-assinar-pre-autorizacao? particular :raio-x 500)) ; false - não precisa porque é urgente
  (pprint (deve-assinar-pre-autorizacao? particular :raio-x 20)) ; false
  (pprint (deve-assinar-pre-autorizacao? plano :raio-x 20)) ; false
  (pprint (deve-assinar-pre-autorizacao? plano :sangue 20))) ; false - não precisa porque é urgente


; essa abordagem anterior não funciona bem porque temos uma mesma regra pra diferentes tipos/classes - copy/paste

; teste: defmulti - define função multipla que tem estratégia diferente de acordo com classe
; não se coloca multi no final, é só pra ter as duas implementações no mesmo arquivo
(defmulti deve-assinar-pre-autorizacao-multi? class)

(defmethod deve-assinar-pre-autorizacao-multi? PacienteParticular [paciente]
  (println "invocando paciente particular")
  true)

(defmethod deve-assinar-pre-autorizacao-multi? PacientePlanoDeSaude [paciente]
  (println "invocando paciente plano de saude")
  false)

; testando
(let [particular (PacienteParticular. 54 "Vitor" "01/04/1991" :urgente)
      plano (PacientePlanoDeSaude. 55 "Vitor" "04/01/1992" :urgente [:raio-x :ultrassom])]
  (pprint (deve-assinar-pre-autorizacao-multi? particular))
  (pprint (deve-assinar-pre-autorizacao-multi? plano)))


; pedido vai ser do tipo: {:paciente paciente :valor valor :procedimento procedimento}
; não dá mais pra diferenciar a implementação de multi por class, porque class do pedido vai ser map

; explorando como funciona a funcao que define a estrategia de um defmulti
(defn minha-funcao [p]
  (println p)
  (class p))

(defmulti multi-teste minha-funcao)
;(multi-teste "vitor")


; pedido vai ser do tipo: {:paciente paciente :valor valor :procedimento procedimento}

; um pouco feio, pois estou misturando keyword e classe como chave
; autorizador é testável, pois é uma função
(defn tipo-de-autorizador [pedido]
  (let [paciente (:paciente pedido)
        situacao (:situacao paciente)
        urgencia? (= :urgente situacao)]
    (if urgencia?
      :sempre-autorizado
      (class paciente))
    ))


(defmulti deve-assinar-pre-autorizacao-do-pedido? tipo-de-autorizador)

(defmethod deve-assinar-pre-autorizacao-do-pedido? :sempre-autorizado [pedido]
  false)

(defmethod deve-assinar-pre-autorizacao-do-pedido? PacienteParticular [pedido]
  (>= (:valor pedido 0) 50))

(defmethod deve-assinar-pre-autorizacao-do-pedido? PacientePlanoDeSaude [pedido]
  (not (some #(= % (:procedimento pedido)) (:plano (:paciente pedido)))))

; testando
(let [particular (PacienteParticular. 54 "Vitor" "01/04/1991" :urgente)
      plano (PacientePlanoDeSaude. 55 "Vitor" "04/01/1992" :urgente [:raio-x :ultrassom])]
  (pprint (deve-assinar-pre-autorizacao-do-pedido? {:paciente particular :valor 1000 :procedimento :coleta-de-sangue}))
  (pprint (deve-assinar-pre-autorizacao-do-pedido? {:paciente plano :valor 1000 :procedimento :coleta-de-sangue})))

(let [particular (PacienteParticular. 54 "Vitor" "01/04/1991" :normal)
      plano (PacientePlanoDeSaude. 55 "Vitor" "04/01/1992" :normal [:raio-x :ultrassom])]
  (pprint (deve-assinar-pre-autorizacao-do-pedido? {:paciente particular :valor 1000 :procedimento :coleta-de-sangue}))
  (pprint (deve-assinar-pre-autorizacao-do-pedido? {:paciente plano :valor 1000 :procedimento :coleta-de-sangue})))


;Nessa aula, aprendemos:
;
;Criar funções múltiplas através do defmulti
;Quebrar a estratégia de acordo com a classe e criar implementações com o uso de defmethods
;Criar definidores de estratégias com o uso do defmulti
;Customizar comportamentos de acordo com tipos e valores
