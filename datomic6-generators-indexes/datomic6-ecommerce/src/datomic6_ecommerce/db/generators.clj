(ns datomic6-ecommerce.db.generators
  (:require [clojure.test.check.generators :as gen]
            [schema-generators.generators :as g]))

; não existe generator de bigdecimal por padrão, precisamos criar
; estratégia: pegar o generator de double e converter pra bigdec usando fmap
(defn double-para-bigdecimal [valor] (BigDecimal. valor))
(def double-finito (gen/double* {:infinite? false, :NaN? false}))
(def bigdecimal (gen/fmap double-para-bigdecimal double-finito))
(def leaf-generators {BigDecimal bigdecimal})

; deu erro inicialmente pq o gen/double pode gerar "including +/- infinity and NaN"
; devo usar gen/double* que tem mais controle (consigo barrar NaN e infinito)