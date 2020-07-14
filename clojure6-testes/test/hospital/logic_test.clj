(ns hospital.logic-test
  (:require [clojure.test :refer :all]
            [hospital.logic :refer :all]
            [hospital.model :as h.model]
            [schema.core :as s]))

(s/set-fn-validation! true)

; run test: TOOLS > REPL > RUN TEST IN CURRENT NS IN REPL

(deftest cabe-na-fila?-test

  ; boundary tests
  ; exatamente na borda e one off (-1, +1, <=, >=, =)

  ; checklist para testes

  ; borda do zero
  (testing "que cabe numa fila vazia"
    (is (cabe-na-fila? {:espera []} :espera)))

  ; borda do limite
  (testing "que não cabe na fila quando a fila está cheia"
    ; é de simples leitura pois é sequencial
    ; mas a desvantagem é que podemos errar em fazer coisas sequenciais

    ; não precisa ser sequencial e no mundo real não é
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

; aula 1
;Nessa aula, aprendemos:
;
;Definir um teste através do deftest
;Utilizar :refer :all para ter acesso a todo o conteúdo de um arquivo
;Criar boundary tests em conjunto com checklists
;Refatorar o código utilizando a macro de threading some->



;(deftest chega-em-test
;
;  (testing "aceita pessoas enquanto cabem pessoas na fila"
;    (is (= {:espera [1 2 3 4 5]}
;           (chega-em {:espera [1 2 3 4]} :espera 5)))
;
;    ; teste não sequencial
;    (is (= {:espera [1 2 5]}
;           (chega-em {:espera [1 2]} :espera 5)))
;    )
;
;  ;(testing "não aceita quando não cabe na fila"
;  ; verificando que uma exception foi jogada
;  ; código clássico horrível. usamos uma exception genérica mas qualquer outro erro
;  ; genérico vai jogar essa exception, e nós vamos achar que deu certo quando deu errado
;  (is (thrown? clojure.lang.ExceptionInfo (chega-em {:espera [1 35 42 64 21]} :espera 76)))
;
;  ; mesmo que eu escolha uma exception do genero, é perigoso
;
;  ; problema: strings de texto solto são super fáceis de quebrar
;  ;(is (thrown-with-msg? clojure.lang.ExceptionInfo  "Não cabe ninguém nesse departamento" (chega-em {:espera [1 35 42 64 21]} :espera 76)))
;
;  ; abordagem: devolver nil - não funciona pra swap
;  ;(is (nil? (chega-em {:espera [1 35 42 64 21]} :espera 76)))
;
;  ; outra maneira de testar, onde ao inves de como java usar o TIPO da exception pra entender o que ocorreu,
;  ; estou uando os dados da exception para isso - ainda sim é uma validação trabalhosa
;  ;(is (try
;  ;      (chega-em {:espera [1 35 42 64 21]} :espera 75)
;  ;      false                                             ; se chegar aqui já deu erro
;  ;      (catch clojure.lang.ExceptionInfo e
;  ;        (= :impossivel-colocar-pessoa-na-fila) (:tipo (ex-data e))))))
;
;  ;(is (= {:hospital {:espera [1 35 42 64 21]} :resultado :impossivel-colocar-pessoa-na-fila}
;  ;       (chega-em {:espera [1 35 42 64 21]} :espera 75)))
;
;  ;)
;  )

(def hospital-original {:espera (conj h.model/fila-vazia "5") :raio-x h.model/fila-vazia})
(def hospital-original-dois {:espera (conj h.model/fila-vazia "51" "5") :raio-x (conj h.model/fila-vazia "13")})
(def hospital-cheio {:espera (conj h.model/fila-vazia "5") :raio-x (conj h.model/fila-vazia "1" "2" "7" "8" "9")})

(deftest transfere-test
  (testing "aceita pessoas se cabe"
    (is (= {:espera [] :raio-x ["5"]}
           (transfere hospital-original :espera :raio-x)))

    (is (= {:espera ["5"] :raio-x ["13" "51"]}
           (transfere hospital-original-dois :espera :raio-x))))

  (testing "recusa pessoas se não cabe"
    (is (thrown? clojure.lang.ExceptionInfo
                 (transfere hospital-cheio :espera :raio-x))))


  ; será que faz sentido eu garantir que o schema está do outro lado?
  ; lembrando que este teste não garante eatamente isso, garante só o erro do nulo
  (testing "Não pode invocar transferência sem hospital"
    (is (thrown? clojure.lang.ExceptionInfo (transfere nil :espera :raio-x)))
    )

  (testing "condições obrigatórias"
    (is (thrown? AssertionError (transfere hospital-original-dois :nao-existe :raio-x)))
    (is (thrown? AssertionError (transfere hospital-original-dois :raio-x :nao-existe)))
    )
  )

; aula 2:
;Nessa aula, aprendemos:
;
;Evitar o uso de valores sequenciais nos testes
;Verificar se uma exception foi lançada
;Implementar o teste de uma maneira otimizada

; aula 3:
;Nessa aula, aprendemos:
;
;Explorar os dados da exception info com ex-data e
;Utilizar os dados da exception para entender o tipo de erro que ocorreu
;Definir as possíveis saídas, os tipos e quando eu quero testar essas saídas

; aula 4:
;Nessa aula, aprendemos:
;
;Utilizar filas ao invés de vetores nos testes
;Trazer schemas para dentro dos testes
;Fazer validações de forma declarativa

; aula 5
;Nessa aula, aprendemos:
;
;Indicar qual tipo de dado está sendo utilizado através do uso da biblioteca Prismatic
;Juntar schemas e testes
;Lidar com exceptions


; aula 6
;Nessa aula, aprendemos:
;
;Restringir o que pode ser passado por parâmetro e o que pode ser retornado
;Lidar com Assertion Errors
;Ter garantias de pré e pós condições
;Criar funções simples para facilitar o teste das Asserções
