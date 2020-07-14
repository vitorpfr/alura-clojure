(ns hospital.logic-test
  (:use clojure.pprint)
  (:require [clojure.test :refer :all]
            [hospital.logic :refer :all]
            [hospital.model :as h.model]
            [schema.core :as s]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer (defspec)]
            [schema-generators.generators :as g]))

(s/set-fn-validation! true)

; run test: TOOLS > REPL > RUN TEST IN CURRENT NS IN REPL

; testes escritos baseados em exemplos
(deftest cabe-na-fila?-test

  (testing "que cabe numa fila vazia"
    (is (cabe-na-fila? {:espera []} :espera)))
  ;
  ;(testing "que numa fila com uma pessoa aleatória cabe mais gente"
  ;  (gen/sample gen/string-alphanumeric 1))

  ; o doseq com um simbolo e uma sequencia gerada funciona
  ; mas talvez não seja o ideal
  (testing "que cabe pessoas em filas de tamanho até 4 inclusive"
    (doseq [fila (gen/sample (gen/vector gen/string-alphanumeric 0 4) 100)]
      (is (cabe-na-fila? {:espera fila} :espera))
      )
    )

  ; borda do limite
  (testing "que não cabe na fila quando a fila está cheia"
    (is (not (cabe-na-fila? {:espera [1 2 3 4 5]} :espera))))

  ; one off da borda do limite pra cima
  (testing "que não cabe na fila quando tem mais do que uma fila cheia"
    (is (not (cabe-na-fila? {:espera [1 2 3 4 5 6]} :espera))))

  ; dentro das bordas
  (testing "que cabe na fila quando tem menos do que uma fila cheia"
    (is (cabe-na-fila? {:espera [1 2 3 4]} :espera))
    (is (cabe-na-fila? {:espera [1 2]} :espera)))

  (testing "que não cabe quando o departamento não existe"
    (is (not (cabe-na-fila? {:espera [1 2 3 4]} :raio-x))))
  )


; Aula 1
;O que aprendemos nesta aula:
;
;Utilizar a biblioteca chamada test check;
;O que é um gen;
;Testar valores dinâmicos;
;Ver inconsistências no nosso código.


; aqui tivemos um problema
; o doseq gera uma multiplicacao de casos, incluindo muitos casos repetidos
;(deftest chega-em-test
;  (testing "Que é colocada uma pessoa em filas menores que 5"
;    (doseq [fila (gen/sample (gen/vector gen/string-alphanumeric 0 4) 10)
;            pessoa (gen/sample gen/string-alphanumeric)]
;      ;(is (cabe-na-fila? {:espera fila} :espera))
;      (println pessoa fila)
;      )))

; o teste a seguir é generativo e funciona
; mas.... o resultado dele parece muito uma cópia do nosso código implementado
; se eu coloquei um bug lá, provavelmente eu coloquei aqui, o teste vai dar true e o bug continua
; não importa se escrevi o teste antes ou depois, é o mesmo código
(defspec coloca-uma-pessoa-em-filas-menores-que-5 100
         (prop/for-all                                      ; como se fosse um let
           [fila (gen/vector gen/string-alphanumeric 0 4)
            pessoa gen/string-alphanumeric]
           (is (= {:espera (conj fila pessoa)}
                  (chega-em {:espera fila} :espera pessoa)))
           ))

; Aula 2
;O que aprendemos nesta aula:
;
;O que o for-all faz;
;O que é o prop;
;Explorar a abordagem de gerar valores e ver se eles são iguais;
;Fazer um teste generativo.


; Property based testing

(def nome-aleatorio-gen
  (gen/fmap clojure.string/join
            (gen/vector gen/char-alphanumeric 5 10)))

(defn transforma-vetor-em-fila [vetor]
  (reduce conj h.model/fila-vazia vetor))

(def fila-nao-cheia-gen
  (gen/fmap
    transforma-vetor-em-fila
    (gen/vector nome-aleatorio-gen 0 4)))

(defn total-de-pacientes [hospital]
  (reduce + (map count (vals hospital))))

; abordagem razoavel, porem horrivel, uma vez que usamos o tipo e o tipo do tipo pra fazer um cond e pegar
; a exception que queremos
; uma alternativa seria usar bibliotecas como a catch-data
; outro problema: rethrow é ruim
;(defn transfere-ignorando-erro [hospital para]
;  (try
;    (transfere hospital :espera para)
;    (catch clojure.lang.ExceptionInfo e
;      (cond
;        (= :fila-cheia (:type (ex-data e))) hospital
;        :else (throw e)
;        )
;      ;(println "falhou" (= :schema.core/error (:type (ex-data e))))
;      ;hospital
;      )))

; abordagem mais interessante pois evita log and rethrow
; mas perde o "poder" de ex-info (ExceptionInfo)
; e ainda tem o problema de que outras partes do meu código podem jogar IllegalStateException
; e eu estou confundindo isso com fila cheia
; para resolver isso, só criando minha propria exception
; mas ai caio no boom de exceptions no ssitema
; ou criar variações de tipos como fizemos no ex-info
(defn transfere-ignorando-erro [hospital para]
  (try
    (transfere hospital :espera para)
    (catch IllegalStateException e
      hospital)))

(defspec transfere-tem-que-manter-a-quantidade-de-pessoas 5
         (prop/for-all
           [;espera gen/string-alphanumeric
            espera (gen/fmap transforma-vetor-em-fila (gen/vector nome-aleatorio-gen 0 50))
            raio-x fila-nao-cheia-gen
            ultrasom fila-nao-cheia-gen
            vai-para (gen/vector (gen/elements [:raio-x :ultrasom]) 0 50)]



           ;(println vai-para)
           (let [hospital-inicial {:espera   espera
                                   :raio-x   raio-x
                                   :ultrasom ultrasom}
                 hospital-final (reduce transfere-ignorando-erro hospital-inicial vai-para)]
             ;(println (count (get hospital-final :raio-x)))
             (= (total-de-pacientes hospital-inicial) (total-de-pacientes hospital-final))
             )
           ))

; Aula 3
;O que aprendemos nesta aula:
;
;Fazer um prop para as propriedades;
;Conceitos sobre teste de propriedade;
;Fazer um Join;
;Testar o nosso fluxo de métodos;

; Aula 4
;O que aprendemos nesta aula:
;
;Ignorar erros dentro dos testes;
;Utilizar o reduce dentro de uma coleção;
;Determinar os valores que vão ser gerados.

; Aula 5
;O que aprendemos nesta aula:
;
;Fazer uma exceção de acordo com os dados dela;
;Utilizar o catch-data;
;Utilizar o maybe;




; generate: devolve um hospital
; generator: devolve um gerador de hospital

(defn adiciona-fila-de-espera [[hospital fila]]
  (assoc hospital :espera fila))

; fmap pega um gerador e aplica uma função no resultado dele
; nesse caso, gera um hospital e adiciona uma fila de espera
(def hospital-gen
  (gen/fmap
    adiciona-fila-de-espera
    (gen/tuple (gen/not-empty (g/generator h.model/Hospital))
               fila-nao-cheia-gen))
  )

(def chega-em-gen
  "gerador de chegadas no hospital"
  (gen/tuple (gen/return chega-em)
             (gen/return :espera)
             nome-aleatorio-gen
             (gen/return 1)))

(defn adiciona-inexistente-ao-departamento [departamento]
  (keyword (str departamento "-inexistente")))

(defn transfere-gen [hospital]
  "gerador de transferencias no hospital"
  (let [departamentos (keys hospital)
        departamentos-inexistentes (map adiciona-inexistente-ao-departamento departamentos)
        todos-os-departamentos (concat departamentos departamentos-inexistentes)]
    (gen/tuple (gen/return transfere)
               (gen/elements todos-os-departamentos)
               (gen/elements todos-os-departamentos)
               (gen/return 0))))

(defn acao-gen [hospital] (gen/one-of [chega-em-gen (transfere-gen hospital)]))

(defn acoes-gen [hospital] (gen/not-empty (gen/vector (acao-gen hospital) 1 100)))

; a sacada do tratamento do erro é que
; estamos criando um teste que valida a propriadede do sistema
; independentemente das ações uma a uam terem sucesso e fracasso
; inclusive com parâmetros inválidos
; aqui inclusive você pode discutir de desativar o schema e o assertion (pre/post) para ver se em execução com
; ele desativado vai manter as propriedades mesmo em situações de erro. super poderoso.
(defn executa-uma-acao [situacao [funcao param1 param2 diferenca-se-sucesso]]
  (let [hospital (:hospital situacao)
        diferenca-atual (:diferenca situacao)]
    (try
      (let [hospital-novo (funcao hospital param1 param2)]
        {:hospital  hospital-novo
         :diferenca (+ diferenca-se-sucesso diferenca-atual)})
      (catch IllegalStateException e
        situacao)

      ; esse é o caso superespecifico, e novamente um caso de erro genérico que ficamos refens d asituacao
      ; mas se a equipe decidir que não é na transferencia que deve ser tratado esse erro, voce poderia
      ; sinalizar o erro de outras maneiras: retorno, outras exceptions
      ; se queremos criar um framework de geração automática de ações e tratamento de erros,
      ; provavelmente você vai ter um padrão de tratamento de erro no sistema
      (catch AssertionError e
        situacao))))

(defspec simula-um-dia-do-hospital-nao-perde-pessoas 50
         (prop/for-all [hospital-inicial hospital-gen]
                       (let [acoes (gen/generate (acoes-gen hospital-inicial))
                             situacao-inicial {:hospital hospital-inicial :diferenca 0}
                             total-de-pacientes-inicial (total-de-pacientes hospital-inicial)
                             situacao-final (reduce executa-uma-acao situacao-inicial acoes)
                             total-de-pacientes-final (total-de-pacientes (:hospital situacao-final))]
                         ;(println total-de-pacientes-final total-de-pacientes-inicial (:diferenca situacao-final))
                         (is (= (- total-de-pacientes-final (:diferenca situacao-final)) total-de-pacientes-inicial)))))


; Aula 6
;O que aprendemos nesta aula:
;
;Criar eventos;
;Utilizar um schema generator;
;Utilizar o g/generate para gerar conforme o schema que temos;
;Utilizar o gen/not-empty;
;Utilizar o one-of;


; Aula 7
;O que aprendemos nesta aula:
;
;Definir o delta para o teste;
;Ver a igualdade dos valores após finalizar o teste;
;Garantir que nossos testes funcionem.
