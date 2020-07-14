(ns hospital.record.aula2
  (:use clojure.pprint))

(defrecord Paciente [id, nome, nascimento])

; Paciente com plano de saude: dados acima + plano de saude
; Paciente sem plano de saude: dados acima

; exemplo ruim de herança, mais parecido com orientação a objeto
; causa problemas e tipos 2^n
;(defrecord PacientePlanoDeSaude HERDA Paciente e adiciona [plano])

; opção: definir dois records diferentes
(defrecord PacienteParticular [id, nome, nascimento])
(defrecord PacientePlanoDeSaude [id, nome, nascimento, plano])


; REGRAS DIFERENTES PARA TIPOS DIFERENTES
;deve-assinar-pre-autorizacao?
;- Particular => se valor >= 50
;- PlanoDeSaude => quando procedimento não está no plano

; código ruim, porque é dificil ler as condições - tudo no mesmo lugar
;(defn deve-assinar-pre-autorizacao [paciente procedimento valor]
;  (if (= PacienteParticular (type paciente))
;    (>= valor 50)
;    ; assumindo que existem os dois tipos
;    (if (= PacientePlanoDeSaude (type paciente))
;      (let [plano (get paciente :plano)]
;        (not (some #(= % procedimento) plano)))
;      true)))

; recurso de clojure: implementar função baseada no tipo
; protocolo: pra saber se um cliente é cobravel, deve checar a funcao deve-assinar-pre-autorizacao
(defprotocol Cobravel
  (deve-assinar-pre-autorizacao? [paciente procedimento valor])
  )

(extend-type PacienteParticular
  Cobravel
  (deve-assinar-pre-autorizacao? [paciente procedimento valor]
    (>= valor 50)))

(extend-type PacientePlanoDeSaude
  Cobravel
  (deve-assinar-pre-autorizacao? [paciente procedimento valor]
    (let [plano (:plano paciente)]
      (not (some #(= % procedimento) plano)))))


(let [particular (PacienteParticular. 54 "Vitor" "01/04/1991")
      plano (PacientePlanoDeSaude. 55 "Vitor" "04/01/1992" [:raio-x :ultrassom])]
  (pprint (deve-assinar-pre-autorizacao? particular :raio-x 500)) ; true - precisa de assinatura porque valor é maior que 50
  (pprint (deve-assinar-pre-autorizacao? particular :raio-x 20)) ; false
  (pprint (deve-assinar-pre-autorizacao? plano :raio-x 20)) ; false
  (pprint (deve-assinar-pre-autorizacao? plano :sangue 20))) ; true - precisa de assinatura porque nao esta no plano


; alternativa: implementar protocol junto com o record
;(defrecord PacientePlanoDeSaude
;  [id nome nascimento plano]
;  Cobravel
;  (deve-assinar-pre-autorizacao? [paciente procedimento valor]
;    (let [plano (:plano paciente)]
;      (not (some #(= % procedimento) plano)))))


; Variação do que é mostrado na palestra a seguir
; From Sean Devlin talk on protocols at Clojure Conj

(defprotocol Dateable
  (to-ms [this]))

(extend-type java.lang.Number
  Dateable
  (to-ms [this] this))

; equal to:
;(extend java.lang.Number
;  Dateable
;  {:to-ms identity})

(pprint (to-ms 56))

(extend-type java.util.Date
  Dateable
  (to-ms [this] (.getTime this)))

(pprint (to-ms (java.util.Date.)))

(extend-type java.util.Calendar
  Dateable
  (to-ms [this] (to-ms (.getTime this))))

(pprint (to-ms (java.util.GregorianCalendar.)))

;Nessa aula, aprendemos:
;
;Utilizar a função some para fazer verificação
;Definir um protocolo com o uso de defprotocol, que se assemelha a interface em Java
;Estender tipo através de extend-type
;Isolar, estender e adicionar comportamentos
;Definir um protocolo Dateable para trabalhar com datas
;Utilizar a função identity para devolver o próprio argumento
