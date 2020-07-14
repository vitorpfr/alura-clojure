(ns hospital.aula3
  (:use clojure.pprint)
  (:require [schema.core :as s]))

(s/set-fn-validation! true)

(def PosInt (s/pred pos-int? 'inteiro-positivo))

(def Paciente
  {:id  PosInt
   :nome s/Str})

(s/defn novo-paciente :- Paciente
  [id :- PosInt
   nome :- s/Str]
  {:id id :nome nome})

(pprint (novo-paciente 15 "Guilherme"))
;(pprint (novo-paciente -15 "Guilherme"))

(defn maior-ou-igual-a-zero [x] (>= x 0))
(def ValorFinanceiro (s/constrained s/Num maior-ou-igual-a-zero))

(def Pedido
  {:paciente     Paciente
   :valor        ValorFinanceiro
   :procedimento s/Keyword})

(s/defn novo-pedido :- Pedido
  [paciente :- Paciente
   valor :- ValorFinanceiro
   procedimento :- s/Keyword]
  {:paciente paciente :valor valor :procedimento procedimento})

(pprint (novo-pedido (novo-paciente 15 "Guilherme") 15.53 :raio-x))
;(pprint (novo-pedido (novo-paciente 15 "Guilherme") -15.53 :raio-x)) ; nao funciona: valor do pedido negativo


(def Numeros [s/Num])
(pprint (s/validate Numeros [15]))
(pprint (s/validate Numeros [15 13]))
(pprint (s/validate Numeros [0]))
;(pprint (s/validate Numeros [nil]))                         ; [nil] nao é numero, faz sentido
(pprint (s/validate Numeros []))
(pprint (s/validate Numeros nil))                           ; nil é considerado um vetor valido


; nil não é s/Num, mas ele é um [s/Num] (vetor de nums)

(def Plano [s/Keyword])
(pprint (s/validate Plano [:raio-x]))

(def Paciente
  {:id  PosInt
   :nome s/Str
   :plano Plano})

(pprint (s/validate Paciente {:id 15 :nome "Guilherme" :plano [:raio-x]}))
(pprint (s/validate Paciente {:id 15 :nome "Guilherme" :plano []}))
(pprint (s/validate Paciente {:id 15 :nome "Guilherme" :plano nil}))

; plano é uma keyword obrigatória no mapa, mas ela pode ter um valor vazio (nil, [])
(pprint (s/validate Paciente {:id 15 :nome "Guilherme"}))


;O que aprendemos nesta aula:
;
;Utilizar uma função para validar os números positivos;
;O que são schemas compostos;
;Fazer a validação em sequência;
;Ter validação com valor obrigatório ou não;
