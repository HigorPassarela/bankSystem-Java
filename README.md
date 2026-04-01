# BankSystem — Sistema Bancário com Microserviços

Sistema bancário completo em Spring Boot 3, Camunda 7, Kafka, Redis e MongoDB.

---

## Subir a infraestrutura

```bash
docker-compose up -d
```

Isso sobe apenas os serviços de infraestrutura:

| Container | Porta | Descrição |
|---|---|---|
| banksystem-mongodb | 27017 | Banco de dados |
| banksystem-redis | 6379 | Cache / saldo atômico |
| banksystem-zookeeper | — | Dependência do Kafka |
| banksystem-kafka | 29092 | Mensageria |
| banksystem-camunda | 8080 | Camunda Cockpit (admin/admin) |
| banksystem-mailhog | 1025 / 8025 | SMTP fake + UI de e-mails |

---

## Rodar os microserviços localmente

Cada serviço é um projeto Spring Boot independente. Em terminais separados:

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

| Serviço | Porta | Swagger |
|---|---|---|
| servico-contas | 8081 | http://localhost:8081/swagger-ui.html |
| servico-transacoes | 8082 | http://localhost:8082/swagger-ui.html |
| servico-extratos | 8083 | http://localhost:8083/swagger-ui.html |
| servico-notificacoes | 8084 | http://localhost:8084/swagger-ui.html |
| servico-fraudes | 8085 | http://localhost:8085/swagger-ui.html |

---

## Variáveis de ambiente (já com defaults para desenvolvimento local)

Os `application.yml` de cada serviço já apontam para `localhost` por padrão.
Nenhuma configuração extra é necessária para rodar localmente após subir o Docker.

| Variável | Padrão |
|---|---|
| MongoDB | mongodb://admin:senha123@localhost:27017/banksystem |
| Redis | localhost:6379 (senha: senha123) |
| Kafka | localhost:29092 |
| MailHog SMTP | localhost:1025 |
| Camunda | http://localhost:8080/engine-rest |

---

## Visualizar e-mails (MailHog)

Acesse **http://localhost:8025** para ver todos os e-mails enviados pelo sistema,
incluindo os links de verificação de conta.

---

## Fluxo principal

1. `POST /api/contas/criar` — cria conta + envia e-mail de verificação
2. Abrir MailHog (http://localhost:8025) e clicar no link de ativação
3. `POST /api/contas/login` — obtém JWT
4. Usar o JWT no Swagger (botão 🔒 Authorize) ou no frontend
5. `POST /api/transacoes/transferencia` — transferência entre contas (requer PIN 4 dígitos)
6. Kafka publica eventos → extratos persistidos no MongoDB → SSE notifica o frontend

---

## Tópicos Kafka criados automaticamente

| Tópico | Produzido por | Consumido por |
|---|---|---|
| `transacoes-aprovadas` | servico-transacoes | servico-extratos, servico-fraudes, servico-notificacoes |
| `transacoes-reprovadas` | servico-transacoes | servico-extratos, servico-notificacoes |

---

## Documentação

- [Endpoints da API](docs/api-endpoints.md)
- [Integração Frontend](docs/frontend-integration.md)
- [Prompt Lovable (geração do frontend)](docs/lovable-prompt.md)
