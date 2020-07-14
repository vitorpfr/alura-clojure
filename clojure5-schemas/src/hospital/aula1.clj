(ns hospital.aula1
  (:use clojure.pprint)
  (:require [schema.core :as s]))

(defn adiciona-paciente [pacientes paciente]
  (if-let [id (:id paciente)]
    (assoc pacientes id paciente)
    (throw (ex-info "Paciente não possui id" {:paciente paciente}))))

; visitas: {15 [], 20 [], 30 []}
(defn adiciona-visita [visitas paciente novas-visitas]
  (if (contains? visitas paciente)
    (update visitas paciente concat novas-visitas)
    (assoc visitas paciente novas-visitas))
  )

(defn imprime-relatorio-de-paciente [visitas paciente]
  (println "Visitas do paciente" paciente "são" (get visitas (:id paciente))))

(defn testa-uso-de-pacientes []
  (let [guilherme {:id 15 :nome "Guilherme"}
        daniela   {:id 20 :nome "Daniela"}
        paulo     {:id 25 :nome "Paulo"}

        ; uma variação com reduce, mais natural
        pacientes (reduce adiciona-paciente {} [guilherme daniela paulo])

        ; uma variação com shadowing, fica feio mesmo
        visitas {}
        visitas (adiciona-visita visitas 15 ["01/01/2019"])
        visitas (adiciona-visita visitas 20 ["01/02/2019", "01/01/2020"])
        visitas (adiciona-visita visitas 15 ["01/03/2019"])]
    (pprint pacientes)
    (pprint visitas)
    (imprime-relatorio-de-paciente visitas guilherme)
    ))

(testa-uso-de-pacientes)

; problema de não usar schema: o simbolo "paciente" está sendo usada diversas vezes
; com significados diferentes

(pprint (s/validate Long 10))
;(pprint (s/validate Long "a"))
;(pprint (s/validate Long [15, 13]))

(s/set-fn-validation! true)

(s/defn teste-simples [x :- Long]
  (println x))
(teste-simples 15)
;(teste-simples "guilherme")


(s/defn imprime-relatorio-de-paciente
  [visitas, paciente :- Long]
  (println "Visitas do paciente" paciente "são" (get visitas (:id paciente))))

; agora conseguimos o erro em tempo de execução que diz que o
; valor passado como parâmetro não condiz com o schema Long
;(testa-uso-de-pacientes)

(s/defn novo-paciente
  [id :- Long, nome :- s/Str]
  {:id id
   :nome nome})

(pprint (novo-paciente 15 "Guilherme"))
(pprint (novo-paciente "Guilherme" 15))

;O que aprendemos nesta aula:
;
;Usar schemas;
;Utilizar a biblioteca de schema plumatic;
;Como colocar dependência em um projeto;
;Validar os valores com base no schema;
;O que é o s/def;
;Verificar o tipo que vamos receber.

;Schemas podem nos ajudar em diversas situações. Qual cenário faz sentido para mantermos nossas validações de schemas?
;
;Alternativa correta
;Ativo em desenvolvimento e testes com boa qualidade, desativado em produção exceto nas camadas de entrada de dados externos.
;
;
;Essa abordagem faz sentido pois temos as garantias dos schemas nas bordas dos sistemas em produção e dentro do sistema em testes com qualidade.
