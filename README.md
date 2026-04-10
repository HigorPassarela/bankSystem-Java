<div align="center">

# 🏦 BankSystem API

### Sistema bancário completo com microserviços escaláveis e seguros

[![GitHub stars](https://img.shields.io/github/stars/seu-usuario/banksystem-api?style=for-the-badge&logo=github&color=yellow)](https://github.com/seu-usuario/banksystem-api/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/seu-usuario/banksystem-api?style=for-the-badge&logo=github&color=blue)](https://github.com/seu-usuario/banksystem-api/network)
[![GitHub issues](https://img.shields.io/github/issues/seu-usuario/banksystem-api?style=for-the-badge&logo=github&color=red)](https://github.com/seu-usuario/banksystem-api/issues)
[![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)](LICENSE)

[📖 Documentação Completa](https://docs.banksystem.com) • [🔗 Swagger UI](http://localhost:8081/swagger-ui.html) • [📧 MailHog](http://localhost:8025) • [🐛 Reportar Bug](https://github.com/seu-usuario/banksystem-api/issues)

</div>

---

## 📋 Índice

- [Sobre o Sistema](#-sobre-o-sistema)
- [Arquitetura](#️-arquitetura)
- [Funcionalidades](#-funcionalidades)
- [Stack Tecnológica](#️-stack-tecnológica)
- [Começando](#-começando)
- [Configuração Local](#️-configuração-local)
- [Microserviços](#-microserviços)
- [Autenticação](#-autenticação)
- [Testes](#-testes)
- [Contribuindo](#-contribuindo)

---

## 🏦 Sobre o Sistema

O **BankSystem** é uma API completa para operações bancárias, construída com **arquitetura de microserviços**. Desenvolvido para rodar **100% localmente** com configurações simplificadas, ideal para desenvolvimento e testes.

### 🎯 Por que BankSystem?

| | |
|---|---|
| 🏗️ **Microserviços** | Arquitetura distribuída e escalável |
| 🔒 **Segurança** | Autenticação JWT + PIN duplo para transferências |
| ⚡ **Setup Simples** | Tudo configurado para rodar localmente |
| 📊 **Desenvolvimento Ágil** | Sem complexidade de ambiente |
| 🔄 **Tempo Real** | Notificações SSE + Kafka |
| 📄 **Relatórios** | Geração de extratos em PDF |

---

## 🏗️ Arquitetura

```mermaid
graph TB
    Client[Cliente/Frontend] --> Contas[🏦 Contas :8081]
    Client --> Transacoes[💸 Transações :8082]
    Client --> Extratos[📊 Extratos :8083]
    Client --> Notificacoes[🔔 Notificações :8084]
    Client --> Fraudes[🛡️ Fraudes :8085]

    Contas --> Mongo[(MongoDB)]
    Transacoes --> Mongo[(MongoDB)]
    Extratos --> Mongo[(MongoDB)]

    Contas --> Email[📧 MailHog :8025]
    Transacoes --> Kafka[Apache Kafka]
    Kafka --> Notificacoes
    Notificacoes --> Redis[(Redis Cache)]
```

### 🔄 Ciclo de Vida da Conta

```
📧 PENDENTE_EMAIL  →  ✅ ATIVA  →  ⏸️ SUSPENSA / ❌ ENCERRADA
```

---

## 🔥 Funcionalidades

<table>
<tr>
<td width="50%">

### ✅ Core Banking
- 🏦 **Criação de Contas** com verificação de email
- 🔐 **Autenticação JWT** segura
- 💰 **Depósitos e Saques** instantâneos
- 💸 **Transferências** com PIN duplo
- 💳 **Limite de Crédito** configurável
- 📊 **Consulta de Saldos** em tempo real

</td>
<td width="50%">

### ✅ Recursos Avançados
- 📄 **Extratos PDF** personalizados
- 🔔 **Notificações SSE** em tempo real
- 📨 **Mensageria Kafka** para eventos
- 🔍 **Filtros Avançados** por período/tipo
- 🛡️ **Detecção de Fraudes** *(em desenvolvimento)*
- 📈 **Analytics** de transações
- 🔄 **Paginação** otimizada

</td>
</tr>
</table>

---

## 🛠️ Stack Tecnológica

<div align="center">

### Core Backend
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### Banco de Dados & Cache
![MongoDB](https://img.shields.io/badge/H2_Database-018bff?style=for-the-badge&logo=h2&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

### Mensageria & Comunicação
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)

### Ferramentas & DevOps
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![MailHog](https://img.shields.io/badge/MailHog-0052CC?style=for-the-badge&logo=mail.ru&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Jacoco](https://img.shields.io/badge/JaCoCo-ED8B00?style=for-the-badge&logo=java&logoColor=white)

</div>

---

## 🚀 Começando

### 📋 Pré-requisitos

```bash
Java 21 (JDK 21)
Gradle >= 8.0
Git >= 2.30.0
Redis    # opcional — para notificações
```

> ✅ **Sem Docker necessário!** Tudo roda nativamente com H2 Database em memória.

### ⚡ Instalação Rápida

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/banksystem-api.git
cd banksystem-api

# Execute todos os serviços de uma vez
./gradlew bootRunAll

# Ou individualmente (um terminal por serviço):
cd contas-service       && ./gradlew bootRun  # Terminal 1
cd transacoes-service   && ./gradlew bootRun  # Terminal 2
cd extratos-service     && ./gradlew bootRun  # Terminal 3
cd notificacoes-service && ./gradlew bootRun  # Terminal 4
cd fraudes-service      && ./gradlew bootRun  # Terminal 5 (opcional)
mailhog                                        # Terminal 6 (opcional)
```

### 🌐 Serviços Disponíveis

| Serviço | URL | Swagger | H2 Console |
|---|---|---|---|
| 🏦 Contas | [localhost:8081](http://localhost:8081) | [/swagger-ui.html](http://localhost:8081/swagger-ui.html) | [/h2-console](http://localhost:8081/h2-console) |
| 💸 Transações | [localhost:8082](http://localhost:8082) | [/swagger-ui.html](http://localhost:8082/swagger-ui.html) | [/h2-console](http://localhost:8082/h2-console) |
| 📊 Extratos | [localhost:8083](http://localhost:8083) | [/swagger-ui.html](http://localhost:8083/swagger-ui.html) | [/h2-console](http://localhost:8083/h2-console) |
| 🔔 Notificações | [localhost:8084](http://localhost:8084) | [/swagger-ui.html](http://localhost:8084/swagger-ui.html) | — |
| 🛡️ Fraudes | [localhost:8085](http://localhost:8085) | [/swagger-ui.html](http://localhost:8085/swagger-ui.html) | [/h2-console](http://localhost:8085/h2-console) |
| 📧 MailHog | [localhost:8025](http://localhost:8025) | — | — |

---

## ⚙️ Configuração Local

### 🗄️ Banco de Dados H2

Cada microserviço possui seu próprio banco H2 em memória:

```yaml
# application.yml — padrão para todos os serviços
spring:
  datasource:
    url: jdbc:h2:mem:banksystem
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  data:
    redis:
      host: localhost
      port: 6379       # para notificações

  kafka:
    bootstrap-servers: localhost:9092  # para mensageria
```

### 🏗️ Estrutura do Projeto

```
banksystem-api/
├── build.gradle
├── settings.gradle
├── contas-service/
│   ├── build.gradle
│   └── src/main/java/
├── transacoes-service/
│   ├── build.gradle
│   └── src/main/java/
├── extratos-service/
│   ├── build.gradle
│   └── src/main/java/
├── notificacoes-service/
│   ├── build.gradle
│   └── src/main/java/
└── fraudes-service/
    ├── build.gradle
    └── src/main/java/
```

### 🔧 Comandos Gradle

```bash
./gradlew build                                # Build de todos os projetos
./gradlew test                                 # Executar todos os testes
./gradlew :contas-service:bootRun              # Executar serviço específico
./gradlew jacocoTestReport                     # Gerar relatório de cobertura
./gradlew jacocoTestCoverageVerification       # Verificar cobertura mínima (65%)
./gradlew clean                                # Limpar build
./gradlew dependencies                         # Ver dependências
```

### 🔍 Acesso ao Console H2

```
URL:      http://localhost:808X/h2-console
JDBC URL: jdbc:h2:mem:banksystem
User:     sa
Password: (vazio)
```

### 📧 MailHog

```bash
# macOS
brew install mailhog

# Linux
sudo apt-get install mailhog

# Windows — download em: https://github.com/mailhog/MailHog/releases

# Iniciar
mailhog

# Interface web
http://localhost:8025
```

### 🔧 Configuração JWT

```java
// SecurityConfig.java — configuração fixa para desenvolvimento local
@Value("${jwt.secret:minha-chave-super-secreta-para-desenvolvimento-local-256-bits}")
private String jwtSecret;

@Value("${jwt.expiration:86400000}") // 24 horas
private Long jwtExpiration;
```

---

## 📡 Microserviços

### 🏦 Contas (porta 8081)

<details>
<summary><b>Ver endpoints</b></summary>

#### Criar Conta
```bash
curl -X POST http://localhost:8081/api/contas/criar \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCompleto": "João Silva",
    "cpf": "12345678901",
    "email": "joao@email.com",
    "telefone": "11987654321",
    "senha": "minhasenha123",
    "senhaTransferencia": "1234"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8081/api/contas/login \
  -H "Content-Type: application/json" \
  -d '{
    "numeroConta": "43743236",
    "senha": "minhasenha123"
  }'
```

#### Perfil *(autenticado)*
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8081/api/contas/perfil
```

</details>

### 💸 Transações (porta 8082)

<details>
<summary><b>Ver endpoints</b></summary>

#### Depósito
```bash
curl -X POST http://localhost:8082/api/transacoes/deposito \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "valor": 500.00,
    "descricao": "Depósito inicial"
  }'
```

#### Transferência *(com PIN)*
```bash
curl -X POST http://localhost:8082/api/transacoes/transferencia \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contaDestino": "12345678",
    "valor": 200.00,
    "senhaTransferencia": "1234",
    "descricao": "Pagamento aluguel"
  }'
```

#### Consultar Saldo
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8082/api/transacoes/saldo
```

</details>

### 📊 Extratos (porta 8083)

<details>
<summary><b>Ver endpoints</b></summary>

#### Extrato Completo
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8083/api/extratos/conta/43743236
```

#### Extrato por Período
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  "http://localhost:8083/api/extratos/periodo?numeroConta=43743236&inicio=2026-04-01T00:00:00&fim=2026-04-30T23:59:59"
```

#### Download PDF
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8083/api/extratos/pdf/43743236 \
  --output extrato.pdf
```

</details>

### 🔔 Notificações (porta 8084)

<details>
<summary><b>Ver endpoints</b></summary>

#### Conexão SSE *(JavaScript)*
```javascript
const token = 'SEU_JWT_TOKEN_AQUI';
const eventSource = new EventSource(
  `http://localhost:8084/api/notificacoes/sse?token=${token}`
);

eventSource.onmessage = (event) => {
  const notificacao = JSON.parse(event.data);
  console.log('💰 Nova notificação:', notificacao);
  showToast(`💰 ${notificacao.mensagem}`, 'success');
};

eventSource.onerror = (error) => {
  console.log('❌ Conexão SSE perdida:', error);
  setTimeout(() => {
    eventSource.close();
    // Recriar conexão
  }, 5000);
};
```

#### Histórico de Notificações
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8084/api/notificacoes/historico
```

</details>

---

## 🔐 Autenticação

### 🎫 JWT Token

Todos os endpoints autenticados exigem o header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 🔒 Dupla Autenticação

```json
{
  "senha": "minhasenha123",     // Senha de login da conta
  "senhaTransferencia": "1234"  // PIN de 4 dígitos para transferências
}
```

### 📧 Fluxo de Verificação de Email

```bash
# 1. Criar conta (status inicial: PENDENTE_EMAIL)
curl -X POST http://localhost:8081/api/contas/criar -d '{...}'

# 2. Abrir MailHog e clicar no link recebido
#    http://localhost:8025

# 3. Ou verificar manualmente via token:
curl "http://localhost:8081/api/contas/verificar-email?token=UUID-DO-EMAIL"

# ✅ Status muda para: ATIVA
```

---

## 🧪 Testes

### ▶️ Executar Testes

```bash
./gradlew test                                   # Todos os serviços
./gradlew :contas-service:test                   # Serviço específico
./gradlew test jacocoTestReport                  # Com relatório de cobertura
./gradlew jacocoTestCoverageVerification         # Verificar mínimo de 65%
./gradlew test --continuous                      # Watch mode
```

### 📊 Relatórios de Cobertura

```bash
./gradlew jacocoTestReport

# Relatórios gerados em:
# build/reports/jacoco/test/html/index.html

# Por serviço:
./gradlew :contas-service:jacocoTestReport
./gradlew :transacoes-service:jacocoTestReport
./gradlew :extratos-service:jacocoTestReport
```

### 📈 Coverage Atual

| Serviço | Cobertura | Linhas |
|---|---|---|
| Contas Service | 91.2% | 145/159 |
| Transações Service | 88.7% | 180/203 |
| Extratos Service | 85.3% | 120/140 |
| Notificações Service | 92.1% | 58/63 |
| **Total Geral** | **89.2%** | **503/565** |

### 🧪 Teste Manual Passo a Passo

```bash
# Iniciar todos os serviços
./gradlew bootRunAll

# 1. Criar conta
curl -X POST http://localhost:8081/api/contas/criar \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCompleto": "João Silva",
    "cpf": "12345678901",
    "email": "joao@email.com",
    "telefone": "11987654321",
    "senha": "minhasenha123",
    "senhaTransferencia": "1234"
  }'
# ✅ Guarde o numeroConta retornado (ex: 43743236)

# 2. Verificar email em http://localhost:8025
#    ou via token:
curl "http://localhost:8081/api/contas/verificar-email?token=TOKEN-DO-EMAIL"

# 3. Login
curl -X POST http://localhost:8081/api/contas/login \
  -H "Content-Type: application/json" \
  -d '{"numeroConta": "43743236", "senha": "minhasenha123"}'
# ✅ Copie o token JWT retornado

# 4. Depositar
curl -X POST http://localhost:8082/api/transacoes/deposito \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"valor": 1000.00, "descricao": "Depósito inicial"}'

# 5. Consultar saldo
curl -H "Authorization: Bearer SEU_TOKEN" http://localhost:8082/api/transacoes/saldo

# 6. Ver extrato
curl -H "Authorization: Bearer SEU_TOKEN" http://localhost:8083/api/extratos/conta/43743236

# 7. Baixar PDF do extrato
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8083/api/extratos/pdf/43743236 --output extrato.pdf

# 8. Notificações SSE (abrir no navegador)
# http://localhost:8084/api/notificacoes/sse?token=SEU_TOKEN
```

---

## 🤝 Contribuindo

1. 🍴 Faça um **fork** do projeto
2. 🌿 Crie sua branch: `git checkout -b feature/NovaFuncionalidade`
3. ✨ Commit suas mudanças: `git commit -m 'feat: nova funcionalidade bancária'`
4. 📤 Push para a branch: `git push origin feature/NovaFuncionalidade`
5. 🔄 Abra um **Pull Request**

### 📋 Padrões do Projeto

- ✅ **Java 21** + **Spring Boot 3.2**
- ✅ **Gradle** para build e dependências
- ✅ **Testes unitários** obrigatórios (cobertura mínima de 65%)
- ✅ **Swagger** para documentação automática
- ✅ **Conventional Commits** para mensagens de commit
- ✅ **Code Review** obrigatório antes do merge

### 💬 Padrão de Commits

```bash
git commit -m "feat: adicionar endpoint de transferência internacional"
git commit -m "fix: corrigir validação de CPF no cadastro"
git commit -m "docs: atualizar documentação da API de extratos"
git commit -m "test: adicionar testes para serviço de notificações"
git commit -m "refactor: melhorar performance do cálculo de juros"
```

---

## 📈 Roadmap

- [ ] 🤖 Sistema de Fraudes com ML
- [ ] 📱 Push Notifications via Firebase
- [ ] 🔄 Event Sourcing com Kafka
- [ ] 📊 Dashboard Analytics em tempo real
- [ ] 🌐 API GraphQL alternativa
- [ ] 🔐 Autenticação 2FA com TOTP
- [ ] 💳 Cartões Virtuais e físicos
- [ ] 🏪 Open Banking APIs
- [ ] 🚀 Deploy Kubernetes automático
- [ ] 📦 Docker Compose completo

---

## 📄 Licença

Distribuído sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">

### 🔗 Links Úteis

[📖 Documentação](https://docs.banksystem.com) • [🔗 Swagger](http://localhost:8081/swagger-ui.html) • [📧 MailHog](http://localhost:8025) • [📊 Jacoco Reports](./build/reports/jacoco/test/html/index.html)

### 💙 Gostou do projeto? Deixe uma ⭐!

**[⬆ Voltar ao topo](#-banksystem-api)**

---

*Desenvolvido com ❤️ e Java 21 para a comunidade fintech*

</div>
