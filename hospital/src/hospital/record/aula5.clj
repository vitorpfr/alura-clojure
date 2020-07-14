(ns hospital.record.aula5
  (:use clojure.pprint))

; ideia é nao trabalhar mais com o tipo da classe, e sim com valores

(defn tipo-de-autorizador [pedido]
  (let [paciente (:paciente pedido)
        situacao (:situacao paciente)]
    (cond (= :urgente situacao)       :sempre-autorizado
          (contains? paciente :plano) :plano-de-saude
          :else                       :credito-minimo)))


(defmulti deve-assinar-pre-autorizacao? tipo-de-autorizador)

(defmethod deve-assinar-pre-autorizacao? :sempre-autorizado [pedido]
  false)

(defmethod deve-assinar-pre-autorizacao? :plano-de-saude [pedido]
  (not (some #(= % (:procedimento pedido)) (:plano (:paciente pedido)))))

(defmethod deve-assinar-pre-autorizacao? :credito-minimo [pedido]
  (>= (:valor pedido 0) 50))


; testando
(let [particular {:id 54 :nome "Vitor" :nascimento "01/04/1991" :situacao :urgente}
      plano {:id 54 :nome "Vitor" :nascimento "01/04/1991" :situacao :urgente :plano [:raio-x :ultrassom]}]
  (pprint (deve-assinar-pre-autorizacao? {:paciente particular :valor 1000 :procedimento :coleta-de-sangue}))
  (pprint (deve-assinar-pre-autorizacao? {:paciente plano :valor 1000 :procedimento :coleta-de-sangue})))

(let [particular {:id 54 :nome "Vitor" :nascimento "01/04/1991" :situacao :normal}
      plano {:id 54 :nome "Vitor" :nascimento "01/04/1991" :situacao :normal :plano [:raio-x :ultrassom]}]
  (pprint (deve-assinar-pre-autorizacao? {:paciente particular :valor 1000 :procedimento :coleta-de-sangue}))
  (pprint (deve-assinar-pre-autorizacao? {:paciente plano :valor 1000 :procedimento :coleta-de-sangue})))
;
;Nessa aula, aprendemos:
;
;Remover a necessidade de records
;Utilizar da função cond para refatorar uma estrutura condicional que utiliza if
;Trabalhar com coleções de valores
;Remover a necessidade de protocols
