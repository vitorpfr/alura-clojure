(ns hospital.aula2
  (:use clojure.pprint)
  (:require [schema.core :as s]))

(s/set-fn-validation! true)

;(s/defrecord Paciente [id :- Long, nome :- s/Str])
;
;(pprint (->Paciente 15 "Guilherme"))
;(pprint (->Paciente "15" "Guilherme"))
;(pprint (map->Paciente {:id "15" :nome "Guilherme" :profissao "Eng"}))

(def Paciente
  "Schema de um paciente"
  {:id s/Num, :nome s/Str})

(pprint (s/explain Paciente))
(pprint (s/validate Paciente {:id 15 :nome "Guilherme"}))

; typo é pego pelo schema, mas poderiamos argumentar que esse
; tipo de erro seria pego em testes automatizados com cobertura boa
; mas... entra a questão de QUERER ser forward-compatible ou não
; entender esse tradeoff
; sistemas externos não me quebrarão ao adicionar campos novos (forward compatible)
; no nosso validate nao estamos sendo forward compatible
; (pode ser interessante se qiisermos analisar mudanças)
;(pprint (s/validate Paciente {:id 15 :name "Guilherme"}))

;(pprint (s/validate Paciente {:id 15 :nome "Guilherme" :plano [:raio-x]}))

; chaves que são keywords em schemas são por padrão OBRIGATORIAS
;(pprint (s/validate Paciente {:id 15}))

; tipo de retorno com schema
; força a validação na saída da função
(s/defn novo-paciente :- Paciente
  [id   :- s/Num
   nome :- s/Str]
  {:id   id
   :nome nome})

(pprint (novo-paciente 15 "Guilherme"))


; função pura
(defn positivo? [x]
  (> x 0))

; criando esquema com uma função
(def EstritamentePositivo (s/pred positivo? 'positivo?))

(pprint (s/validate EstritamentePositivo 15))
;(pprint (s/validate EstritamentePositivo 0))
;(pprint (s/validate EstritamentePositivo -15))


(def Paciente
  "Schema de um paciente"
  {:id (s/constrained s/Int pos?), :nome s/Str,})
; é por isso que é importante debulhar documentação
; já existe pos? e já existe pos-int?
; dica é sempre explorar documentação

(pprint (s/validate Paciente {:id 15 :nome "Guilherme"}))
;(pprint (s/validate Paciente {:id -15 :nome "Guilherme"}))

; um caminho que eu não seguiria: lambdas dentro dos schemas
; os nomes ficam confusos ou a legibilidade do schema se perde
; você também perde a facildiade de testar o lambda isoladamente
;(def Paciente
;  "Schema de um paciente"
;  {:id (s/constrained s/Int #(> % 0)), :nome s/Str,})
;
;(pprint (s/validate Paciente {:id 15 :nome "Guilherme"}))
;(pprint (s/validate Paciente {:id 0 :nome "Guilherme"}))
;; usar lambda não é uma boa prática aqui, porque na mensagem de debug a função não tem nome

; validação imperativa: s/validate
; validação declarativa: colocar schemas no input e output das funções



;O que aprendemos nesta aula:
;
;O que é s/defrecord é expansível e imutável;
;O que é explain;
;Criar schemas para números;
;O que é s/pred;
;Desativar ou ativar validação;
;Criar restrições;
;Utilizar o pos;
