(ns hospital.aula5
  (:use clojure.pprint)
  (:require [schema.core :as s]))

(s/set-fn-validation! true)

; modelo
(def PosInt (s/pred pos-int? 'inteiro-positivo))
(def Plano [s/Keyword])

(def Paciente
  {:id                          PosInt
   :nome                        s/Str
   :plano                       Plano
   (s/optional-key :nascimento) s/Str})

(def Pacientes
  {PosInt Paciente})

(def Visitas
  {PosInt [s/Str]})

;(s/validate Paciente {:id nil :nome "Guilherme" :plano []}) ; nil não é posint
; granhamos a garantia de que existe um id valido, se a validacao tiver ativa


; teste, refatorando código pra usar schema
; removi o if, removi o throw, pois o esquema garantiu a existencia do id e a validade do id
; se a validação estiver ativa
(s/defn adiciona-paciente :- Pacientes
  [pacientes :- Pacientes
   paciente :- Paciente]
  (let [id (:id paciente)]
    (assoc pacientes id paciente)))

(s/defn adiciona-visita :- Visitas
  [visitas :- Visitas
   paciente :- PosInt
   novas-visitas :- [s/Str]]
  (if (contains? visitas paciente)
    (update visitas paciente concat novas-visitas)
    (assoc visitas paciente novas-visitas))
  )

(s/defn imprime-relatorio-de-paciente
  [visitas :- Visitas
   paciente :- PosInt]
  (println "Visitas do paciente" paciente "são" (get visitas paciente)))

(defn testa-uso-de-pacientes []
  (let [guilherme {:id 15 :nome "Guilherme" :plano []}
        daniela {:id 20 :nome "Daniela" :plano []}
        paulo {:id 25 :nome "Paulo" :plano []}

        ; uma variação com reduce, mais natural
        pacientes (reduce adiciona-paciente {} [guilherme daniela paulo])

        ; uma variação com shadowing, fica feio mesmo
        visitas {}
        visitas (adiciona-visita visitas 15 ["01/01/2019"])
        visitas (adiciona-visita visitas 20 ["01/02/2019", "01/01/2020"])
        visitas (adiciona-visita visitas 15 ["01/03/2019"])]
    (pprint pacientes)
    (pprint visitas)
    (imprime-relatorio-de-paciente visitas 20)
    ))

(testa-uso-de-pacientes)


;O que aprendemos nesta aula:
;
;Utilizar o modelo de schema;
;Garantir que nosso o schema está validando nosso modelo;
