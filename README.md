# BankSystem — Sistema Bancário com Microserviços

Sistema bancário completo em Spring Boot 3, Camunda 7, Kafka, Redis e MongoDB.

---

## Subir a infraestrutura + serviços

Build .jar para baixar imagens no Docker:
```bash
cd servico-contas && ./gradlew clean bootJar -x test
cd ../servico-transacoes && ./gradlew clean bootJar -x test 
cd ../servico-extratos && ./gradlew clean bootJar -x test 
cd ../servico-notificacoes && ./gradlew clean bootJar -x test 
cd ../servico-fraudes && ./gradlew clean bootJar -x test 
cd ..
```

Subir todos os servicos junto a infraestrutura:
```bash
docker compose up -d --build
```

Caso queira recriar os serviços por conta de atualização:
```bash
docker compose up -d --force-recreate
```
---
## Subir apenas a infraestrutura

obs: lembre-se de tirar os serviços do docker-compose para isso!
```bash
docker-compose up -d
```

Isso sobe apenas os serviços de infraestrutura:

| Container | Porta | Descrição |
|---|---|---|
| banksystem-mongodb | 27017 | Banco de dados principal |
| banksystem-redis | 6379 | Cache / saldo atômico em centavos |
| banksystem-zookeeper | — | Dependência interna do Kafka |
| banksystem-kafka | 29092 | Mensageria entre microserviços |
| banksystem-camunda | 8080 | Motor de processos BPMN (admin/admin) |
| banksystem-mailhog | 1025 / 8025 | SMTP fake + UI de e-mails |

---

## Rodar os microserviços localmente

Cada serviço é um projeto Spring Boot independente. Abra um terminal para cada um:

```bash
# Serviço de Contas
cd servico-contas
./gradlew bootRun

# Serviço de Transações
cd servico-transacoes
./gradlew bootRun

# Serviço de Extratos
cd servico-extratos
./gradlew bootRun

# Serviço de Notificações
cd servico-notificacoes
./gradlew bootRun

# Serviço de Fraudes
cd servico-fraudes
./gradlew bootRun
```

### Portas dos microserviços

| Serviço | Porta | Swagger UI |
|---|---|---|
| servico-contas | 8081 | http://localhost:8081/swagger-ui.html |
| servico-transacoes | 8082 | http://localhost:8082/swagger-ui.html |
| servico-extratos | 8083 | http://localhost:8083/swagger-ui.html |
| servico-notificacoes | 8084 | http://localhost:8084/swagger-ui.html |
| servico-fraudes | 8085 | http://localhost:8085/swagger-ui.html |

> Em todos os Swagger UIs, clique em **Authorize 🔒** e cole `Bearer <token>` para autenticar os endpoints protegidos.

## Arquitetura do projeto
```
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
---

## Variáveis de ambiente

Os `application.yml` de cada serviço já apontam para `localhost` por padrão.
Nenhuma configuração extra é necessária para rodar localmente após subir o Docker.

| Variável | Padrão |
|---|---|
| MongoDB | `mongodb://admin:senha123@localhost:27017/banksystem` |
| Redis | `localhost:6379` (senha: `senha123`) |
| Kafka | `localhost:29092` |
| MailHog SMTP | `localhost:1025` |
| Camunda REST | `http://localhost:8080/engine-rest` |

---

## Visualizar e-mails (MailHog)

Acesse **http://localhost:8025** para ver todos os e-mails enviados pelo sistema.
O link de verificação de conta chega aqui após o cadastro.

---

## Ciclo de vida da conta

```
Cadastro → PENDENTE_EMAIL → (clica no link do MailHog) → ATIVA → SUSPENSA / ENCERRADA
```

- Contas com status `PENDENTE_EMAIL` **não conseguem fazer login**.
- Apenas contas `ATIVA` podem realizar transações.

---

## Fluxo principal

1. `POST /api/contas/criar` — cria conta (status: `PENDENTE_EMAIL`), envia e-mail de verificação
2. Acessar **http://localhost:8025** (MailHog) e clicar no link de ativação
3. Conta muda para status `ATIVA` no MongoDB
4. `POST /api/contas/login` — obtém token JWT
5. Usar o JWT no Swagger (🔒 Authorize) ou no frontend
6. `POST /api/transacoes/deposito` — depositar saldo na conta
7. `POST /api/transacoes/debito` / `credito` — movimentar saldo/limite
8. `POST /api/transacoes/transferencia` — transferir entre contas (requer PIN de 4 dígitos)
9. Kafka publica eventos → MongoDB persiste → SSE notifica o frontend em tempo real
10. `GET /api/extratos/pdf/{numeroConta}` — baixar extrato em PDF

---

## Tópicos Kafka criados automaticamente

| Tópico | Produzido por | Consumido por |
|---|---|---|
| `transacoes-aprovadas` | servico-transacoes | servico-extratos, servico-fraudes, servico-notificacoes |
| `transacoes-reprovadas` | servico-transacoes | servico-extratos, servico-notificacoes |

---

## Senhas — dois tipos distintos

| Tipo | Tamanho | Uso |
|---|---|---|
| Senha de login | 6–50 caracteres | Autenticar no sistema (`POST /login`) |
| Senha de transferência (PIN) | exatamente 4 dígitos | Autorizar transferências entre contas |

As duas senhas são independentes e armazenadas com BCrypt.

---

## Documentação

- [Endpoints da API](docs/api-endpoints.md)