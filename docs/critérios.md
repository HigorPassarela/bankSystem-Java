# Critérios de Apresentação — Projeto Final

Este guia foi pensado para maximizar sua nota nos critérios de Demo, Arquitetura, Perguntas técnicas e Organização/tempo — que juntos valem 1,5 pontos — e para reforçar a percepção de qualidade nos critérios de código (6,5 pontos).

## Estrutura sugerida

O tempo base é 10 minutos. Se precisar de mais tempo, é possível estender até 15 minutos conforme a coluna de extensão abaixo.

| Bloco | 10 min (base) | 15 min (extensão) | O que cobrir |
|-------|---------------|-------------------|--------------|
| Abertura + Arquitetura | 2 min | 3 min | Problema de negócio + diagrama de serviços |
| Demo ao vivo | 6 min | 8 min | Fluxo completo ponta a ponta |
| Decisões técnicas | — | 2 min | Um ponto relevante por componente |
| Testes + Encerramento | 2 min | 2 min | JaCoCo, reflexão final |

**Regra de ouro para 10 minutos:** se algum bloco atrasar, corte da demo, não do encerramento. Terminar com reflexão demonstra maturidade; terminar no meio de um log de console não.

## Bloco 1 — Abertura + Arquitetura (2 min / 3 min na extensão)

Não comece pelo código. Comece pelo porquê e mostre o mapa antes de entrar no território.

### Abertura (~40 seg):
- "Nosso projeto simula uma plataforma financeira que processa transações com segurança, notifica o cliente em tempo real e gera extrato em PDF."
- Mencione a stack: Spring Boot, Kafka, Redis, MongoDB, Camunda, SSE.

### Diagrama de arquitetura (~1 min 20 seg):
Apresente um único diagrama com todos os serviços e suas conexões:

```
[Cliente HTTP] → [Serviço de Tx] → [Redis]
                        ↓
                    [Kafka]
                        ↓
                    [Camunda]
                   ↙        ↘
[Serviço de Notificações]   [Serviço de Faturas/Extrato]
            ↓                           ↓
           SSE                      [MongoDB]
            ↓
    [Frontend React]
```

- Use cores para separar camadas: transporte (Kafka/SSE), persistência (Redis/Mongo), orquestração (Camunda).
- Mostre a direção dos dados com setas.
- Não explique cada serviço em detalhe — a demo faz isso. Aqui é só o mapa.

## Bloco 2 — Demo ao vivo (6 min / 8 min na extensão)

⭐ **Este é o bloco mais importante.** Siga um roteiro fixo e ensaie pelo menos duas vezes antes.

Para caber em 6 minutos, cada passo tem ~1 minuto. Não tente mostrar tudo — mostre uma coisa que funciona por componente.

### Roteiro de demo (6 min)

#### 1. Transação + Atomicidade — 1 min
- POST via Postman ou curl. Mostre no log que a operação de débito é atômica — o ponto não é o Redis em si, mas a garantia de atomicidade.
- Redis é a abordagem prioritária (via DECRBY); o MongoDB também suporta isso via findAndModify, mas Redis é a escolha principal.
- Se tiver cenário de saldo insuficiente: demonstre o bloqueio em 10 segundos.

#### 2. Kafka — 1 min
- Mostre no log do consumidor que o evento chegou no tópico correto.
- Fale o nome do tópico em voz alta — isso conta na avaliação.

#### 3. Camunda — 1,5 min
- Abra o Camunda Cockpit com o processo já em andamento (não espere subir ao vivo).
- Mostre pelo menos uma transição de estado no diagrama BPMN.
- Se tiver tratamento de falha, é o momento de mencionar.

#### 4. MongoDB + PDF — 1 min
- Mostre o documento de fatura no Compass ou via API.
- Faça o download do PDF — abrir o arquivo ao vivo causa impacto.

#### 5. SSE — 1,5 min
- Tenha o frontend React já aberto no browser antes de começar.
- Execute uma transação e mostre a notificação chegando sem refresh.
- Este é o momento mais visual — deixe o público ver a tela.

### Se a extensão for liberada (8 min)
Use os 2 minutos extras para aprofundar o Camunda (mostrar erro e compensação) ou o Redis (mostrar a chave sendo criada e expirada no Redis CLI).

### Checklist de preparação para o dia
- [ ] Todos os serviços rodando (Docker Compose testado do zero na véspera)
- [ ] Postman Collection com as requisições nomeadas e em ordem
- [ ] Camunda Cockpit aberto e logado com um processo já iniciado
- [ ] MongoDB Compass ou endpoint de consulta pronto
- [ ] Frontend rodando, SSE já conectado
- [ ] Terminal com logs visíveis (fonte ≥ 14pt, fundo escuro)
- [ ] PDF já gerado e salvo — não dependa de gerar ao vivo se for lento
- [ ] Plano B: vídeo gravado da demo completa caso algo falhe

## Bloco 3 — Decisões técnicas (apenas na extensão — 2 min)

Se o tempo for extendido para 15 min, use este bloco para explicar uma decisão por componente em uma frase focada no porquê, não no como:

| Componente | Exemplo de decisão a destacar |
|------------|------------------------------|
| Redis | "A prioridade é garantir atomicidade no débito — Redis é a escolha principal via DECRBY, que é atômico por natureza. MongoDB também suporta atomicidade via findAndModify, mas Redis é nossa abordagem prioritária" |
| Kafka | "Chave de partição = contaId para garantir ordem de eventos por conta" |
| Camunda | "Usamos User Task para aprovação manual de transações suspeitas e incidentes para tratar falhas no fluxo sem derrubar o processo" |
| MongoDB | "Aggregation pipeline para consolidar o extrato antes de renderizar o PDF" |
| SSE | "SseEmitter com timeout configurado e reconexão automática no frontend" |

Na versão de 10 min, essas decisões devem ser ditas dentro da demo, em uma frase rápida ao mostrar cada componente.

## Bloco 4 — Testes + Encerramento (2 min)

### Testes (~1 min):
- Mostre o relatório de cobertura (JaCoCo ou relatório de cobertura da IDE) — precisa estar ≥ 65%.
- Mencione o tipo de teste priorizado: unitário, integração.
- Se estiver abaixo de 65%, diga o que cobriu — não esconda.

### Encerramento (~1 min):
- O que foi mais difícil e como resolveu.
- O que faria diferente com mais tempo.
- Essa reflexão alimenta diretamente a nota do professor (avaliação subjetiva, peso ×0,10).

## Preparação para perguntas técnicas

As perguntas serão feitas ao final de todas as apresentações — mas esteja pronto para responder perguntas no meio da sua também, caso o professor ou os convidados interrompam. Prepare respostas para os componentes de maior peso:

O professor e os convidados podem perguntar sobre qualquer componente. As perguntas abaixo são as mais prováveis — conheça as respostas, não as decore.

### Redis (×0,20 — maior peso)
O ponto central não é o Redis em si — é a garantia de atomicidade. Redis é a solução prioritária, mas o MongoDB também viabiliza operações atômicas. Saiba defender a escolha técnica, não apenas o nome da tecnologia.

- Como você garantiu atomicidade na operação de débito?
- Por que Redis é a escolha prioritária para isso? O MongoDB não resolveria?
- O que acontece se o Redis cair durante a transação?
- Por que não banco relacional com SELECT FOR UPDATE?

### Camunda (×0,15)
- Qual é o modelo BPMN do seu processo? Mostre no Cockpit.
- Como você tratou falhas? Tem User Task ou incidente no diagrama?
- Como o Camunda se comunica com os outros serviços?

### Kafka (×0,10)
- Qual é o nome do tópico? Quantas partições?
- O consumidor é idempotente? O que acontece se a mesma mensagem chegar duas vezes?
- Você usou chave de partição? Por quê?

### MongoDB (×0,10)
- Como está modelado o documento de fatura?
- Usou índices? Quais campos?
- Como gerou o PDF? Qual biblioteca?

### SSE (×0,05)
- O que é SSE e por que não usou WebSocket?
- Como o frontend reconecta se a conexão cair?

## Dicas gerais

- **Ensaie o cronômetro:** faça uma apresentação completa com timer. Saber onde você está no tempo evita cortes de última hora.
- **Logs visíveis:** mostrar o sistema funcionando nos logs vale tanto quanto a demo visual.
- **Não peça desculpas** por funcionalidades faltando — explique a priorização.
- **Fale sobre o que não está pronto** antes de o professor ou os convidados perguntarem — demonstra maturidade técnica.
- **A apresentação é individual** — você apresenta tudo sozinho. Ensaie os componentes que domina menos para não travar.
---

```
Além disso explique a estrutura completa do projeto e me de uma aula completa sobre o projeto, aqui está a estrutura, se precisar mando os controllers e as classes, mas preciso saber completamente como funciona o projeto de ponta a ponta e o que todos os serviços fazem: 

estrutura:
Listagem de caminhos de pasta para o volume Windows
O n·mero de sÚrie do volume Ú E0A7-1B74
C:.
+---.idea
ª   +---modules
+---camunda-workflows
ª   +---processos
+---docs
+---servico-contas
ª   +---.gradle
ª   ª   +---8.14
ª   ª   ª   +---checksums
ª   ª   ª   +---executionHistory
ª   ª   ª   +---expanded
ª   ª   ª   +---fileChanges
ª   ª   ª   +---fileHashes
ª   ª   ª   +---vcsMetadata
ª   ª   +---buildOutputCleanup
ª   ª   +---vcs-1
ª   +---build
ª   ª   +---classes
ª   ª   ª   +---java
ª   ª   ª       +---main
ª   ª   ª       ª   +---br
ª   ª   ª       ª       +---com
ª   ª   ª       ª           +---banksystem
ª   ª   ª       ª               +---contas
ª   ª   ª       ª                   +---config
ª   ª   ª       ª                   +---controller
ª   ª   ª       ª                   +---dto
ª   ª   ª       ª                   +---exception
ª   ª   ª       ª                   +---mapper
ª   ª   ª       ª                   +---model
ª   ª   ª       ª                   ª   +---dto
ª   ª   ª       ª                   +---repository
ª   ª   ª       ª                   +---security
ª   ª   ª       ª                   +---service
ª   ª   ª       +---test
ª   ª   ª           +---br
ª   ª   ª               +---com
ª   ª   ª                   +---banksystem
ª   ª   ª                       +---contas
ª   ª   ª                           +---controller
ª   ª   ª                           +---dto
ª   ª   ª                           +---exception
ª   ª   ª                           +---mapper
ª   ª   ª                           +---model
ª   ª   ª                           +---repository
ª   ª   ª                           +---security
ª   ª   ª                           +---service
ª   ª   +---generated
ª   ª   ª   +---sources
ª   ª   ª       +---annotationProcessor
ª   ª   ª       ª   +---java
ª   ª   ª       ª       +---main
ª   ª   ª       ª       +---test
ª   ª   ª       +---headers
ª   ª   ª           +---java
ª   ª   ª               +---main
ª   ª   ª               +---test
ª   ª   +---jacoco
ª   ª   +---libs
ª   ª   +---reports
ª   ª   ª   +---jacoco
ª   ª   ª   ª   +---test
ª   ª   ª   ª       +---html
ª   ª   ª   ª           +---br.com.banksystem.contas
ª   ª   ª   ª           +---br.com.banksystem.contas.config
ª   ª   ª   ª           +---br.com.banksystem.contas.controller
ª   ª   ª   ª           +---br.com.banksystem.contas.dto
ª   ª   ª   ª           +---br.com.banksystem.contas.exception
ª   ª   ª   ª           +---br.com.banksystem.contas.mapper
ª   ª   ª   ª           +---br.com.banksystem.contas.model
ª   ª   ª   ª           +---br.com.banksystem.contas.model.dto
ª   ª   ª   ª           +---br.com.banksystem.contas.security
ª   ª   ª   ª           +---br.com.banksystem.contas.service
ª   ª   ª   ª           +---jacoco-resources
ª   ª   ª   +---problems
ª   ª   ª   +---tests
ª   ª   ª       +---test
ª   ª   ª           +---classes
ª   ª   ª           +---css
ª   ª   ª           +---js
ª   ª   ª           +---packages
ª   ª   +---resources
ª   ª   ª   +---main
ª   ª   +---test-results
ª   ª   ª   +---test
ª   ª   ª       +---binary
ª   ª   +---tmp
ª   ª       +---.cache
ª   ª       ª   +---expanded
ª   ª       ª       +---zip_9892ccb804f78c0637616b68610d363f
ª   ª       ª           +---META-INF
ª   ª       ª           ª   +---maven
ª   ª       ª           ª       +---org.jacoco
ª   ª       ª           ª           +---org.jacoco.agent
ª   ª       ª           +---org
ª   ª       ª               +---jacoco
ª   ª       ª                   +---agent
ª   ª       +---bootJar
ª   ª       +---compileJava
ª   ª       ª   +---compileTransaction
ª   ª       ª       +---backup-dir
ª   ª       ª       +---stash-dir
ª   ª       +---compileTestJava
ª   ª       ª   +---compileTransaction
ª   ª       ª       +---backup-dir
ª   ª       ª       +---stash-dir
ª   ª       +---test
ª   +---gradle
ª   ª   +---wrapper
ª   +---src
ª       +---main
ª       ª   +---java
ª       ª   ª   +---br
ª       ª   ª       +---com
ª       ª   ª           +---banksystem
ª       ª   ª               +---contas
ª       ª   ª                   +---config
ª       ª   ª                   +---controller
ª       ª   ª                   +---dto
ª       ª   ª                   +---exception
ª       ª   ª                   +---mapper
ª       ª   ª                   +---model
ª       ª   ª                   ª   +---dto
ª       ª   ª                   +---repository
ª       ª   ª                   +---security
ª       ª   ª                   +---service
ª       ª   +---resources
ª       +---test
ª           +---java
ª               +---br
ª                   +---com
ª                       +---banksystem
ª                           +---contas
ª                               +---controller
ª                               +---dto
ª                               +---exception
ª                               +---mapper
ª                               +---model
ª                               +---repository
ª                               +---security
ª                               +---service
+---servico-extratos
ª   +---.gradle
ª   ª   +---8.14
ª   ª   ª   +---checksums
ª   ª   ª   +---executionHistory
ª   ª   ª   +---expanded
ª   ª   ª   +---fileChanges
ª   ª   ª   +---fileHashes
ª   ª   ª   +---vcsMetadata
ª   ª   +---buildOutputCleanup
ª   ª   +---vcs-1
ª   +---build
ª   ª   +---classes
ª   ª   ª   +---java
ª   ª   ª       +---main
ª   ª   ª       ª   +---br
ª   ª   ª       ª       +---com
ª   ª   ª       ª           +---banksystem
ª   ª   ª       ª               +---extratos
ª   ª   ª       ª                   +---config
ª   ª   ª       ª                   +---controller
ª   ª   ª       ª                   +---dto
ª   ª   ª       ª                   +---exception
ª   ª   ª       ª                   +---kafka
ª   ª   ª       ª                   +---mapper
ª   ª   ª       ª                   +---model
ª   ª   ª       ª                   +---repository
ª   ª   ª       ª                   +---security
ª   ª   ª       ª                   +---service
ª   ª   ª       +---test
ª   ª   ª           +---br
ª   ª   ª               +---com
ª   ª   ª                   +---banksystem
ª   ª   ª                       +---extratos
ª   ª   ª                           +---controller
ª   ª   ª                           +---dto
ª   ª   ª                           +---exception
ª   ª   ª                           +---kafka
ª   ª   ª                           +---mapper
ª   ª   ª                           +---model
ª   ª   ª                           +---repository
ª   ª   ª                           +---security
ª   ª   ª                           +---service
ª   ª   +---generated
ª   ª   ª   +---sources
ª   ª   ª       +---annotationProcessor
ª   ª   ª       ª   +---java
ª   ª   ª       ª       +---main
ª   ª   ª       ª       +---test
ª   ª   ª       +---headers
ª   ª   ª           +---java
ª   ª   ª               +---main
ª   ª   ª               +---test
ª   ª   +---jacoco
ª   ª   +---libs
ª   ª   +---reports
ª   ª   ª   +---jacoco
ª   ª   ª   ª   +---test
ª   ª   ª   ª       +---html
ª   ª   ª   ª           +---br.com.banksystem.extratos
ª   ª   ª   ª           +---br.com.banksystem.extratos.config
ª   ª   ª   ª           +---br.com.banksystem.extratos.controller
ª   ª   ª   ª           +---br.com.banksystem.extratos.dto
ª   ª   ª   ª           +---br.com.banksystem.extratos.exception
ª   ª   ª   ª           +---br.com.banksystem.extratos.kafka
ª   ª   ª   ª           +---br.com.banksystem.extratos.mapper
ª   ª   ª   ª           +---br.com.banksystem.extratos.model
ª   ª   ª   ª           +---br.com.banksystem.extratos.security
ª   ª   ª   ª           +---br.com.banksystem.extratos.service
ª   ª   ª   ª           +---jacoco-resources
ª   ª   ª   +---problems
ª   ª   ª   +---tests
ª   ª   ª       +---test
ª   ª   ª           +---classes
ª   ª   ª           +---css
ª   ª   ª           +---js
ª   ª   ª           +---packages
ª   ª   +---resources
ª   ª   ª   +---main
ª   ª   +---test-results
ª   ª   ª   +---test
ª   ª   ª       +---binary
ª   ª   +---tmp
ª   ª       +---.cache
ª   ª       ª   +---expanded
ª   ª       ª       +---zip_9892ccb804f78c0637616b68610d363f
ª   ª       ª           +---META-INF
ª   ª       ª           ª   +---maven
ª   ª       ª           ª       +---org.jacoco
ª   ª       ª           ª           +---org.jacoco.agent
ª   ª       ª           +---org
ª   ª       ª               +---jacoco
ª   ª       ª                   +---agent
ª   ª       +---bootJar
ª   ª       +---compileJava
ª   ª       ª   +---compileTransaction
ª   ª       ª       +---backup-dir
ª   ª       ª       +---stash-dir
ª   ª       +---compileTestJava
ª   ª       ª   +---compileTransaction
ª   ª       ª       +---backup-dir
ª   ª       ª       +---stash-dir
ª   ª       +---test
ª   +---gradle
ª   ª   +---wrapper
ª   +---src
ª       +---main
ª       ª   +---java
ª       ª   ª   +---br
ª       ª   ª       +---com
ª       ª   ª           +---banksystem
ª       ª   ª               +---extratos
ª       ª   ª                   +---config
ª       ª   ª                   +---controller
ª       ª   ª                   +---dto
ª       ª   ª                   +---exception
ª       ª   ª                   +---kafka
ª       ª   ª                   +---mapper
ª       ª   ª                   +---model
ª       ª   ª                   +---repository
ª       ª   ª                   +---security
ª       ª   ª                   +---service
ª       ª   +---resources
ª       +---test
ª           +---java
ª               +---br
ª                   +---com
ª                       +---banksystem
ª                           +---extratos
ª                               +---controller
ª                               +---dto
ª                               +---exception
ª                               +---kafka
ª                               +---mapper
ª                               +---model
ª                               +---repository
ª                               +---security
ª                               +---service
+---servico-fraudes
ª   +---.gradle
ª   ª   +---8.14
ª   ª   ª   +---checksums
ª   ª   ª   +---executionHistory
ª   ª   ª   +---expanded
ª   ª   ª   +---fileChanges
ª   ª   ª   +---fileHashes
ª   ª   ª   +---vcsMetadata
ª   ª   +---buildOutputCleanup
ª   ª   +---vcs-1
ª   +---build
ª   ª   +---classes
ª   ª   ª   +---java
ª   ª   ª       +---main
ª   ª   ª           +---br
ª   ª   ª               +---com
ª   ª   ª                   +---banksystem
ª   ª   ª                       +---fraudes
ª   ª   ª                           +---config
ª   ª   ª                           +---delegate
ª   ª   ª                           +---dto
ª   ª   ª                           +---kafka
ª   ª   ª                           +---service
ª   ª   +---generated
ª   ª   ª   +---sources
ª   ª   ª       +---annotationProcessor
ª   ª   ª       ª   +---java
ª   ª   ª       ª       +---main
ª   ª   ª       +---headers
ª   ª   ª           +---java
ª   ª   ª               +---main
ª   ª   +---libs
ª   ª   +---reports
ª   ª   ª   +---problems
ª   ª   +---resources
ª   ª   ª   +---main
ª   ª   ª       +---processos
ª   ª   +---tmp
ª   ª       +---bootJar
ª   ª       +---compileJava
ª   +---gradle
ª   ª   +---wrapper
ª   +---src
ª       +---main
ª           +---java
ª           ª   +---br
ª           ª       +---com
ª           ª           +---banksystem
ª           ª               +---fraudes
ª           ª                   +---config
ª           ª                   +---delegate
ª           ª                   +---dto
ª           ª                   +---kafka
ª           ª                   +---service
ª           +---resources
ª               +---processos
+---servico-notificacoes
ª   +---.gradle
ª   ª   +---8.14
ª   ª   ª   +---checksums
ª   ª   ª   +---executionHistory
ª   ª   ª   +---expanded
ª   ª   ª   +---fileChanges
ª   ª   ª   +---fileHashes
ª   ª   ª   +---vcsMetadata
ª   ª   +---buildOutputCleanup
ª   ª   +---vcs-1
ª   +---build
ª   ª   +---classes
ª   ª   ª   +---java
ª   ª   ª       +---main
ª   ª   ª           +---br
ª   ª   ª               +---com
ª   ª   ª                   +---banksystem
ª   ª   ª                       +---notificacoes
ª   ª   ª                           +---config
ª   ª   ª                           +---controller
ª   ª   ª                           +---dto
ª   ª   ª                           +---kafka
ª   ª   ª                           +---security
ª   ª   ª                           +---service
ª   ª   +---generated
ª   ª   ª   +---sources
ª   ª   ª       +---annotationProcessor
ª   ª   ª       ª   +---java
ª   ª   ª       ª       +---main
ª   ª   ª       +---headers
ª   ª   ª           +---java
ª   ª   ª               +---main
ª   ª   +---libs
ª   ª   +---reports
ª   ª   ª   +---problems
ª   ª   +---resources
ª   ª   ª   +---main
ª   ª   +---tmp
ª   ª       +---bootJar
ª   ª       +---compileJava
ª   +---gradle
ª   ª   +---wrapper
ª   +---src
ª       +---main
ª           +---java
ª           ª   +---br
ª           ª       +---com
ª           ª           +---banksystem
ª           ª               +---notificacoes
ª           ª                   +---config
ª           ª                   +---controller
ª           ª                   +---dto
ª           ª                   +---kafka
ª           ª                   +---security
ª           ª                   +---service
ª           +---resources
+---servico-transacoes
    +---.gradle
    ª   +---8.14
    ª   ª   +---checksums
    ª   ª   +---executionHistory
    ª   ª   +---expanded
    ª   ª   +---fileChanges
    ª   ª   +---fileHashes
    ª   ª   +---vcsMetadata
    ª   +---buildOutputCleanup
    ª   +---vcs-1
    +---build
    ª   +---classes
    ª   ª   +---java
    ª   ª       +---main
    ª   ª       ª   +---br
    ª   ª       ª       +---com
    ª   ª       ª           +---banksystem
    ª   ª       ª               +---transacoes
    ª   ª       ª                   +---client
    ª   ª       ª                   +---config
    ª   ª       ª                   +---controller
    ª   ª       ª                   +---dto
    ª   ª       ª                   +---exception
    ª   ª       ª                   +---kafka
    ª   ª       ª                   +---security
    ª   ª       ª                   +---service
    ª   ª       +---test
    ª   ª           +---br
    ª   ª               +---com
    ª   ª                   +---banksystem
    ª   ª                       +---transacoes
    ª   ª                           +---client
    ª   ª                           +---controller
    ª   ª                           +---dto
    ª   ª                           +---exception
    ª   ª                           +---kafka
    ª   ª                           +---security
    ª   ª                           +---service
    ª   +---generated
    ª   ª   +---sources
    ª   ª       +---annotationProcessor
    ª   ª       ª   +---java
    ª   ª       ª       +---main
    ª   ª       ª       +---test
    ª   ª       +---headers
    ª   ª           +---java
    ª   ª               +---main
    ª   ª               +---test
    ª   +---jacoco
    ª   +---libs
    ª   +---reports
    ª   ª   +---jacoco
    ª   ª   ª   +---test
    ª   ª   ª       +---html
    ª   ª   ª           +---br.com.banksystem.transacoes
    ª   ª   ª           +---br.com.banksystem.transacoes.client
    ª   ª   ª           +---br.com.banksystem.transacoes.config
    ª   ª   ª           +---br.com.banksystem.transacoes.controller
    ª   ª   ª           +---br.com.banksystem.transacoes.dto
    ª   ª   ª           +---br.com.banksystem.transacoes.exception
    ª   ª   ª           +---br.com.banksystem.transacoes.kafka
    ª   ª   ª           +---br.com.banksystem.transacoes.security
    ª   ª   ª           +---br.com.banksystem.transacoes.service
    ª   ª   ª           +---jacoco-resources
    ª   ª   +---problems
    ª   ª   +---tests
    ª   ª       +---test
    ª   ª           +---classes
    ª   ª           +---css
    ª   ª           +---js
    ª   ª           +---packages
    ª   +---resources
    ª   ª   +---main
    ª   +---test-results
    ª   ª   +---test
    ª   ª       +---binary
    ª   +---tmp
    ª       +---.cache
    ª       ª   +---expanded
    ª       ª       +---zip_9892ccb804f78c0637616b68610d363f
    ª       ª           +---META-INF
    ª       ª           ª   +---maven
    ª       ª           ª       +---org.jacoco
    ª       ª           ª           +---org.jacoco.agent
    ª       ª           +---org
    ª       ª               +---jacoco
    ª       ª                   +---agent
    ª       +---bootJar
    ª       +---compileJava
    ª       ª   +---compileTransaction
    ª       ª       +---backup-dir
    ª       ª       +---stash-dir
    ª       +---compileTestJava
    ª       ª   +---compileTransaction
    ª       ª       +---backup-dir
    ª       ª       +---stash-dir
    ª       +---test
    +---gradle
    ª   +---wrapper
    +---src
        +---main
        ª   +---java
        ª   ª   +---br
        ª   ª       +---com
        ª   ª           +---banksystem
        ª   ª               +---transacoes
        ª   ª                   +---client
        ª   ª                   +---config
        ª   ª                   +---controller
        ª   ª                   +---dto
        ª   ª                   +---exception
        ª   ª                   +---kafka
        ª   ª                   +---security
        ª   ª                   +---service
        ª   +---resources
        +---test
            +---java
                +---br
                    +---com
                        +---banksystem
                            +---transacoes
                                +---client
                                +---controller
                                +---dto
                                +---exception
                                +---kafka
                                +---security
                                +---service
```