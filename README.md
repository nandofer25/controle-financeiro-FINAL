# Sistema de Controle Financeiro (Spring Boot + Singleton + Adapter + Proxy + Strategy + Observer + State)

API REST de **despesas, receitas, orcamentos por categoria e alertas de gasto**. Trabalho bimestral: padrao **Singleton** aplicado a um logger (`AppLogger`), seguido dos padroes **Adapter** e **Proxy** na consulta de CEP e, agora, dos padroes comportamentais **Strategy**, **Observer** e **State** na nova funcionalidade de **orcamento mensal por categoria**.

- Java 21 · Spring Boot 3.3.5 · Spring Data JPA · H2 (em memoria) · Maven

## Como rodar no IntelliJ
1. `File > Open` e selecione a pasta `controle-financeiro` (o IntelliJ le o `pom.xml` e baixa as dependencias).
2. Confira o JDK 21 em `File > Project Structure > Project SDK`.
3. Rode `ControleFinanceiroApplication` (botao verde). A API sobe em `http://localhost:8080`.
4. Abra `requests.http` e execute as requisicoes (ou use Postman/curl).
5. Testes: `mvn test` (ou botao direito na pasta `src/test` > Run). Rodam: `AppLoggerTest`, `CepAdapterProxyTest`, `EstrategiaLimiteTest`, `EstadoOrcamentoTest`, `PublicadorTransacaoTest` e `OrcamentoObserverTest`.
6. O console H2 fica em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:financeiro`, user `sa`, senha vazia).

O arquivo `app.log` e criado/atualizado na **raiz do projeto**. O `app.log` incluido e um exemplo.

## Rotas (recurso: `Transacao`)
| Metodo | Rota | Retorno |
|---|---|---|
| POST | `/transacoes` | 201 Created (+ header Location) |
| GET | `/transacoes` | 200 OK (lista) |
| GET | `/transacoes/{id}` | 200 OK ou 404 |
| PUT | `/transacoes/{id}` | 200 OK ou 404 |
| DELETE | `/transacoes/{id}` | 204 No Content ou 404 |
| GET | `/transacoes/resumo` | 200 OK (extra: receitas, despesas, saldo e `alertaGasto`) |

Corpo de exemplo:
```json
{ "descricao": "Aluguel", "valor": 1200.00, "tipo": "DESPESA", "categoria": "Moradia" }
```
`tipo` = `RECEITA` ou `DESPESA`. Dados invalidos retornam 400.

**Alerta de gasto:** despesa acima de `financeiro.limite-alerta-despesa` (500.00 em `application.properties`) gera uma linha `ALERTA DE GASTO` no log (agora feito por um *Observer*, ver mais abaixo); `/transacoes/resumo` retorna `alertaGasto=true` quando despesas > receitas.

## Rotas de orcamento (nova funcionalidade)
| Metodo | Rota | Retorno |
|---|---|---|
| POST | `/orcamentos` | 201 Created (+ Location), 400 dados invalidos, 409 orcamento duplicado |
| GET | `/orcamentos?ano=2026&mes=11` | 200 OK (filtros opcionais), com consumo, limite efetivo e status |
| GET | `/orcamentos/{id}` | 200 OK ou 404 |
| PUT | `/orcamentos/{id}/limite` | 200 OK, 404 ou **409** (orcamento encerrado) |
| POST | `/orcamentos/{id}/encerrar` | 200 OK, 404 ou **409** (ja encerrado) |
| POST | `/orcamentos/{id}/reabrir` | 200 OK, 404 ou **409** (nao esta encerrado) |
| DELETE | `/orcamentos/{id}` | 204 No Content ou 404 |
| GET | `/alertas` | 200 OK (alertas gerados pelos orcamentos, mais recentes primeiro) |

Corpo do POST:
```json
{ "categoria": "Alimentacao", "ano": 2026, "mes": 11, "tipoLimite": "FIXO", "valorLimite": 1000.00 }
```
`tipoLimite` = `FIXO` (`valorLimite` em reais) ou `PERCENTUAL_DA_RECEITA` (`valorLimite` de 0 a 100, % das receitas do mes).

## Estrutura
```
logger/AppLogger.java            Singleton classico (sem Spring)  <- VERSAO FINAL
logger/Nivel.java                DEBUG, INFO, ERRO
logger/spring/AppLoggerSpring    Copia como @Component (so ativa com o profile "spring-logger")
model/Transacao, TipoTransacao, ResumoFinanceiro
repository/TransacaoRepository  (JPA)
service/TransacaoService         (regras + logs)
controller/TransacaoController, GlobalExceptionHandler
controller/CepController         GET /cep/{cep} (so conhece a interface ConsultaCep)
cep/ConsultaCep                  Interface do nosso sistema
cep/Endereco                     Objeto do nosso sistema (cep, logradouro, bairro, cidade, estado)
cep/adapter/ViaCepAdapter        ADAPTER: JSON da ViaCEP -> Endereco
cep/adapter/BrasilApiAdapter     ADAPTER: JSON da BrasilAPI -> Endereco
cep/provedor/FonteJson           De onde vem o JSON bruto (HTTP real ou simulado)
cep/provedor/HttpFonteJson       GET real no provedor (HttpClient do Java)
cep/provedor/ViaCepSimulado      JSON fixo no formato da ViaCEP (sem internet)
cep/provedor/BrasilApiSimulado   JSON fixo no formato da BrasilAPI (sem internet)
cep/ConsultaCepProxy             PROXY: valida, usa cache e loga CACHE x PROVEDOR
cep/CepConfig                    Monta tudo (unico lugar que sabe qual provedor esta ativo)

orcamento/Orcamento              Entidade = CONTEXTO do State (guarda so o status e delega ao estado)
orcamento/OrcamentoService       Regras do orcamento: orquestra Strategy + State (sem if/switch de tipo ou status)
orcamento/estrategia/*           STRATEGY: EstrategiaLimite, LimiteFixo, LimitePercentualReceita, SeletorEstrategiaLimite
orcamento/estado/*               STATE: EstadoOrcamento, EstadoNormal, EstadoAtencao, EstadoEstourado, EstadoEncerrado, EstadoOrcamentoFactory
orcamento/AlertaOrcamento(+Repository)  Alerta gravado quando o orcamento ENTRA em ATENCAO/ESTOURADO
evento/SujeitoTransacao          OBSERVER: interface do Subject (registrar, remover, notificar)
evento/PublicadorTransacao       OBSERVER: Subject feito na mao (lista de observadores, isola falhas)
evento/ObservadorTransacao       OBSERVER: interface dos observadores
evento/AlertaLimiteDespesaObserver   Observador: alerta de despesa individual alta (regra que saiu do TransacaoService)
evento/OrcamentoObserver         Observador: reavalia os orcamentos afetados por uma transacao
controller/OrcamentoController   Rotas /orcamentos e /alertas (so chama o Service)
```

## Consulta de CEP: Adapter + Proxy

`GET /cep/{cep}` (8 digitos, sem hifen). Retorno: 200 (`Endereco`), 400 (formato invalido), 404 (CEP nao existe) ou 502 (provedor fora do ar).

```json
{ "cep": "01001000", "logradouro": "Praca da Se", "bairro": "Se", "cidade": "Sao Paulo", "estado": "SP" }
```

**Fluxo:** `CepController` -> `ConsultaCepProxy` -> `ViaCepAdapter` ou `BrasilApiAdapter` -> `FonteJson` (HTTP ou simulada).

**Trocar de provedor (so propriedade, em `application.properties`):**
```properties
cep.provedor=viacep      # ou brasilapi
cep.modo=real            # ou simulado (JSON fixo, funciona sem internet)
```
CEPs que existem nos provedores simulados: `01001000` e `89010025`. Qualquer outro retorna 404.

**Adapter:** cada provedor devolve um JSON diferente (ViaCEP: `localidade`/`uf`, com hifen no CEP; BrasilAPI: `city`/`state`, sem hifen). Cada adapter traduz para o mesmo `Endereco` e implementa `ConsultaCep`; fora do pacote `cep` ninguem sabe qual provedor esta em uso e nao existe `if` por provedor. O unico ponto de decisao e a `CepConfig`, guiada pela propriedade.

**Proxy:** `ConsultaCepProxy` implementa a mesma interface e (1) bloqueia CEP que nao tenha exatamente 8 digitos antes de chamar o provedor, (2) guarda em um `ConcurrentHashMap` os CEPs ja consultados (so sucessos) e (3) loga se a resposta veio do `CACHE` ou do `PROVEDOR`. Feito na mao: sem `@Cacheable`. O `CepConfig` registra o Proxy como bean `@Primary` do tipo `ConsultaCep`, entao o Controller o recebe sem saber.

Exemplo de log (`app.log`):
```
[INFO] ConsultaCepProxy - [Proxy] CEP 01001000 nao esta no cache -> consultando o PROVEDOR
[INFO] ViaCepAdapter - [Adapter] JSON da ViaCEP traduzido para Endereco
[INFO] ConsultaCepProxy - [Proxy] CEP 01001000 -> resposta veio do PROVEDOR e foi guardada no cache (1 no cache)
[INFO] ConsultaCepProxy - [Proxy] CEP 01001000 -> resposta veio do CACHE
[ERRO] ConsultaCepProxy - [Proxy] CEP BLOQUEADO: formato invalido (esperado: 8 digitos). Provedor nao foi chamado
```
O valor do CEP invalido nao e escrito no log de proposito (uma requisicao poderia embutir quebra de linha e forjar linhas no `app.log`).

Testes: `mvn test` roda `CepAdapterProxyTest` (adapters com os dois formatos, cache, bloqueio de formato, erro nao fica em cache).

## Nova funcionalidade: orcamento mensal por categoria + alertas de gasto

**Problema que resolve.** Antes, o sistema so avisava de uma despesa individual alta e de despesas maiores que receitas no total. Agora a pessoa define **quanto pretende gastar por categoria em cada mes** (ex.: Alimentacao R$ 1.000 ou Lazer = 10% da renda do mes) e o sistema acompanha o consumo a cada lancamento, mudando a situacao do orcamento (`NORMAL`, `ATENCAO`, `ESTOURADO`, `ENCERRADO`) e gerando alertas quando ele esquenta ou estoura. Nao e CRUD puro: ha regras de calculo de limite, transicoes de estado, operacoes que dependem do estado e reacao automatica a mudancas nas transacoes.

**Regras de negocio**
- Consumo = soma das despesas da categoria (sem diferenciar maiusculas/minusculas) dentro do mes do orcamento.
- `NORMAL`: consumo < 80% do limite. `ATENCAO`: de 80% ate 100%. `ESTOURADO`: acima de 100%. Sem nenhum gasto, sempre `NORMAL`.
- Alerta so e gerado na **transicao** (ao entrar em `ATENCAO` ou `ESTOURADO`), para nao repetir o mesmo aviso a cada lancamento.
- Criar, editar ou excluir uma transacao reavalia sozinho os orcamentos afetados (inclusive a categoria/mes antigos quando o lancamento muda de lugar).
- Uma receita altera o limite dos orcamentos `PERCENTUAL_DA_RECEITA` do mes, entao todos eles sao reavaliados.
- `ENCERRADO` congela o orcamento: novos lancamentos nao alteram o status, nao e possivel mudar o limite nem encerrar de novo. `reabrir` so vale para orcamento encerrado e recalcula o status com o que foi lancado nesse meio tempo.

**Fluxo**
```
TransacaoController -> TransacaoService --grava--> notifica SujeitoTransacao (PublicadorTransacao)
                                                      |-> AlertaLimiteDespesaObserver  (log "ALERTA DE GASTO")
                                                      '-> OrcamentoObserver -> OrcamentoService
                                                              |- SeletorEstrategiaLimite -> EstrategiaLimite  (STRATEGY: calcula o limite)
                                                              '- Orcamento -> EstadoOrcamento               (STATE: decide a transicao)
                                                                      '-> grava AlertaOrcamento + AppLogger
```

### Padrao 1 - Strategy (limite do orcamento)
- **Por que:** o limite pode ser calculado de formas diferentes (valor fixo ou percentual da renda do mes). Sem o padrao, o Service teria `if (tipo == FIXO) ... else if (tipo == PERCENTUAL) ...`, e cada nova forma de calcular (ex.: media dos ultimos 3 meses) obrigaria a editar o Service.
- **Como:** `EstrategiaLimite` (interface) com `LimiteFixo` e `LimitePercentualReceita`. O `SeletorEstrategiaLimite` recebe do Spring a lista de todas as estrategias e escolhe pelo `TipoLimite`, sem `if`. O `OrcamentoService` so chama `estrategia.calcular(...)`.
- **Estender:** criar uma classe `@Component` que implemente `EstrategiaLimite` e um valor novo em `TipoLimite`. Nenhuma outra classe muda (coberto por `novaEstrategiaEntraSemMudarOSeletor`).

### Padrao 2 - Observer (reagir a mudancas nas transacoes)
- **Por que:** varias coisas precisam acontecer quando uma transacao muda (alerta de despesa alta, atualizar orcamentos...). Sem o padrao, o `TransacaoService` teria que conhecer e chamar cada uma, misturando responsabilidades e crescendo a cada novidade.
- **Como:** `SujeitoTransacao` / `PublicadorTransacao` (Subject feito na mao, com `registrar`, `remover` e `notificar`, **sem** `ApplicationEventPublisher`/`@EventListener`) e `ObservadorTransacao` (interface). O evento `EventoTransacao` leva uma copia imutavel (`DadosTransacao`) do antes e depois, nao a entidade JPA. O `TransacaoService` so chama `eventos.notificar(...)` depois de gravar.
- **Robustez:** falha em um observador e registrada no `AppLogger` e nao derruba a operacao nem os outros observadores, entao o CRUD continua funcionando.
- **Estender:** criar um `@Component` que implemente `ObservadorTransacao` (ex.: enviar e-mail). O Service nao muda.

### Padrao 3 - State (ciclo de vida do orcamento)
- **Por que:** o que se pode fazer com um orcamento depende da situacao dele (nao se reabre o que ja esta aberto, nem se altera limite de orcamento encerrado), e a proxima situacao depende do consumo. Sem o padrao, o Service teria `switch(status)` repetido em varios metodos.
- **Como:** `Orcamento` (contexto) guarda o `StatusOrcamento` (enum persistido) e delega `atualizarConsumo`, `encerrar`, `reabrir`, `aoAlterarLimite` e `mensagemDeEntrada` ao objeto `EstadoOrcamento` correspondente (`EstadoOrcamentoFactory`). `EstadoNormal`, `EstadoAtencao` e `EstadoEstourado` (base `EstadoAberto`) classificam o consumo e geram sua mensagem de alerta; `EstadoEncerrado` ignora consumo e veta alteracoes. Operacao invalida lanca `OperacaoInvalidaException` -> **HTTP 409**.

### Principios SOLID aplicados
- **S:** `TransacaoService` cuida so de transacoes; calcular limite (Strategy), decidir transicoes (State), reagir a eventos (Observer) e orquestrar orcamentos (`OrcamentoService`) ficam em classes separadas. Controllers so chamam o Service.
- **O:** nova forma de limite = nova `EstrategiaLimite`; nova reacao a transacao = novo `ObservadorTransacao`; nenhum Service precisa ser editado.
- **L:** qualquer `EstrategiaLimite`, `ObservadorTransacao` ou `EstadoOrcamento` pode substituir outro sem o chamador perceber (os testes usam implementacoes anonimas).
- **I:** interfaces pequenas e focadas (`EstrategiaLimite` tem 2 metodos, `ObservadorTransacao` tem 1).
- **D:** `TransacaoService` depende de `SujeitoTransacao` (interface), `OrcamentoService` depende de `SeletorEstrategiaLimite`; a montagem fica com o Spring. Nao ha dependencia circular: `TransacaoService` -> `SujeitoTransacao` -> observadores -> `OrcamentoService` -> repositorios.

### Integracao com o que ja existia
- **CRUD:** as rotas de `/transacoes` continuam iguais (mesmos status e corpos). A unica mudanca interna e que o alerta de despesa alta saiu do Service para um observador, com a mesma mensagem de log.
- **Singleton Logger:** todas as classes novas usam `AppLogger.getInstance()` (nivel DEBUG para detalhes, INFO para operacoes e transicoes, ERRO para 404/409/400 e falhas de observador).
- **Adapter e Proxy:** `/cep/{cep}` nao foi alterado.

### Melhorias em relacao a versao anterior
- O sistema deixa de apenas avisar de gasto isolado e passa a controlar metas por categoria e mes, com historico em `/alertas`.
- `TransacaoService` ficou menor e sem regra de alerta embutida; adicionar comportamento novo nao exige alteracao nele.
- Regras de status concentradas em classes de estado testaveis sem banco nem Spring (25 testes unitarios novos para Strategy, State e Observer).

### Decisoes e dificuldades de projeto
- **Persistir o estado:** o estado e guardado como enum (`StatusOrcamento`) e convertido para o objeto de estado por uma factory; assim o JPA continua simples e as classes de estado nao tem dados.
- **Evitar spam de alertas:** alerta so na transicao, comparando o status antes e depois da reavaliacao.
- **Orcamento congelado x consulta:** `ENCERRADO` nao muda por novos lancamentos, mas `GET /orcamentos/{id}` sempre mostra o consumo real; ao `reabrir`, o status e recalculado.
- **Lancamento que muda de categoria/mes:** o evento leva tambem o estado anterior para reavaliar os dois orcamentos.
- **Isolar falhas dos observadores** para nao quebrar o CRUD.

### Roteiro de demonstracao (requests.http, itens 18 a 37)
1. Criar orcamento FIXO de R$ 1.000 para Alimentacao e um de 10% da receita para Lazer.
2. Lancar uma receita e despesas de Alimentacao (700, 150, 300) e acompanhar `NORMAL -> ATENCAO -> ESTOURADO` em `GET /orcamentos/1` e os alertas em `GET /alertas` e no `app.log`.
3. Encerrar o orcamento e mostrar os 409 (encerrar de novo, mudar limite), e reabrir recalculando.
4. Mostrar os 400 (percentual acima de 100, corpo invalido) e 404.

Exemplo de log (`app.log`):
```
[DEBUG] PublicadorTransacao - [Observer] evento CRIADA (transacao id=3) para 2 observador(es)
[INFO] OrcamentoService - Orcamento id=1 mudou de estado: NORMAL -> ATENCAO
[INFO] OrcamentoService - ALERTA DE ORCAMENTO: Atencao: categoria 'Alimentacao' consumiu 850.00 de 1000.00 (80% ou mais do limite)
```

## Niveis de log usados
- **DEBUG**: detalhes da operacao (ex.: "Criando transacao: tipo=..., valor=...").
- **INFO**: operacao concluida (ex.: "Transacao criada com sucesso: id=1") e alertas de gasto.
- **ERRO**: recurso nao encontrado (404), validacao (400) e excecoes (500).
- **Dados sensiveis:** nunca sao registrados. O corpo da requisicao nao e logado; so campos de negocio (tipo, categoria, valor) e ids. Excecoes registram apenas tipo e mensagem.

## Como trocar de versao (opcional)
Para usar a versao Spring no lugar da classica, no `TransacaoService`:
1. Troque o campo `private final AppLogger log = AppLogger.getInstance();` por `private final AppLoggerSpring log;`
2. Receba-o no construtor: `public TransacaoService(TransacaoRepository repository, AppLoggerSpring log, @Value(...) BigDecimal limite)` e atribua `this.log = log;`
3. Rode com `--spring.profiles.active=spring-logger` (IntelliJ: *Run Configuration > Active profiles*).

---

## Respostas (Parte 3)

**Quem garante a instancia unica em cada versao?**
Na versao classica, a propria classe: construtor privado (ninguem faz `new`), campo estatico `volatile` e `getInstance()` com dupla checagem em `synchronized`.
Na versao `@Component`, o container do Spring: o escopo padrao dos beans e *singleton*, entao ele cria um unico objeto e injeta o mesmo em quem pedir. A classe em si nao impede `new`.

**Quando o Singleton classico ainda faz sentido?**
Quando o objeto precisa existir fora do container do Spring ou antes dele subir (metodo `main`, utilitarios estaticos, bibliotecas, projetos sem Spring).
Tambem quando se quer que a unicidade seja garantida pela propria classe, independente de framework.

### Justificativa da versao final
A versao final usa o **Singleton classico (`AppLogger`)**. Motivos: (1) o logger e usado ate no `main`, antes de o contexto do Spring existir, onde um bean ainda nao pode ser injetado; (2) o mesmo `getInstance()` serve ao Service, ao Controller e ao `GlobalExceptionHandler` sem acoplar o logger ao framework; (3) a unicidade fica garantida pela propria classe, comprovada no `AppLoggerTest` (construtor privado, mesma instancia, 50 threads simultaneas). Em contrapartida, o `@Component` e mais facil de mockar em testes e de configurar por injecao, e seria a escolha natural se o projeto crescesse e o logger passasse a depender de configuracoes do Spring.
