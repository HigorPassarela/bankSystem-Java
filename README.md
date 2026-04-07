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

Subir apenas infraestrutura:
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
- [Integração Frontend](docs/frontend-integration.md)
- [Prompt Lovable (geração do frontend)](docs/lovable-prompt.md)