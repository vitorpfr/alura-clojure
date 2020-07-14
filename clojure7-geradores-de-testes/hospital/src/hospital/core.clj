(ns hospital.core
  (:use clojure.pprint)
  (:require [clojure.test.check.generators :as gen]
            [hospital.model :as h.model]
            [schema-generators.generators :as g]))


(println (gen/sample gen/boolean 3))
(println (gen/sample gen/int 100))
(println (gen/sample gen/string))
(println (gen/sample gen/string-alphanumeric))
(println (gen/sample gen/string-alphanumeric 5))


(println (gen/sample (gen/vector gen/int 15) 5))
(println (gen/sample (gen/vector gen/int) 100))


; generators do schema deduz generators a partir do schema

; sample: gera sequencia de tamanho n
(println (g/sample 10 h.model/Paciente))
(pprint (g/sample 10 h.model/Departamento))
(pprint (g/sample 10 h.model/Hospital))

; generate: gera um elemento
(println "gerando com generate")
(pprint (g/generate h.model/Hospital))
