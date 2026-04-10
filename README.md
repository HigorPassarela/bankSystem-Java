```markdown
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

- [🏦 Sobre o Sistema](#-sobre-o-sistema)
- [🏗️ Arquitetura](#️-arquitetura)
- [🔥 Funcionalidades](#-funcionalidades)
- [🛠️ Stack Tecnológica](#️-stack-tecnológica)
- [🚀 Começando](#-começando)
- [⚙️ Configuração Local](#️-configuração-local)
- [📡 Microserviços](#-microserviços)
- [🔐 Autenticação](#-autenticação)
- [🧪 Testes](#-testes)
- [🤝 Contribuindo](#-contribuindo)

---

## 🏦 Sobre o Sistema

O **BankSystem** é uma API completa para operações bancárias, construída com **arquitetura de microserviços**. Desenvolvido para rodar **100% localmente** com configurações simplificadas, ideal para desenvolvimento e testes.

### 🎯 Por que BankSystem?

- 🏗️ **Microserviços**: Arquitetura distribuída e escalável
- 🔒 **Segurança**: Autenticação JWT + PIN duplo para transferências
- ⚡ **Setup Simples**: Tudo configurado para rodar localmente
- 📊 **Desenvolvimento Ágil**: Sem complexidade de ambiente
- 🔄 **Tempo Real**: Notificações SSE + Kafka
- 📄 **Relatórios**: Geração de extratos em PDF

---

## 🏗️ Arquitetura

<div align="center">

```mermaid
graph TB
    Client[Cliente/Frontend] --> Contas[🏦 Contas :8081]
    Client --> Transacoes[💸 Transações :8082]
    Client --> Extratos[📊 Extratos :8083]
    Client --> Notificacoes[🔔 Notificações :8084]
    Client --> Fraudes[🛡️ Fraudes :8085]
    
    Contas --> H2_1[(H2 Database)]
    Transacoes --> H2_2[(H2 Database)]
    Extratos --> H2_3[(H2 Database)]
    
    Contas --> Email[📧 MailHog :8025]
    Transacoes --> Kafka[Apache Kafka]
    Kafka --> Notificacoes
    Notificacoes --> Redis[(Redis Cache)]
```

</div>

### 🎯 Ciclo de Vida da Conta

```
📧 PENDENTE_EMAIL → ✅ ATIVA → ⏸️ SUSPENSA / ❌ ENCERRADA
```

---

## 🔥 Funcionalidades

<table>
<tr>
<td width="50%">

### ✅ Core Banking
- 🏦 **Criação de Contas** com verificação email
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
- 🛡️ **Detecção de Fraudes** (em desenvolvimento)
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
![H2](https://img.shields.io/badge/H2_Database-018bff?style=for-the-badge&logo=h2&logoColor=white)
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
Redis (opcional - para notificações)
```

> ✅ **Sem Docker necessário!** Tudo roda nativamente com H2 Database em memória

### ⚡ Instalação Super Rápida

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/banksystem-api.git
cd banksystem-api

# Execute todos os serviços de uma vez
./gradlew bootRunAll

# Ou execute cada serviço individualmente:
# Terminal 1 - Contas
cd contas-service && ./gradlew bootRun

# Terminal 2 - Transações  
cd transacoes-service && ./gradlew bootRun

# Terminal 3 - Extratos
cd extratos-service && ./gradlew bootRun

# Terminal 4 - Notificações
cd notificacoes-service && ./gradlew bootRun

# Terminal 5 - Fraudes (opcional)
cd fraudes-service && ./gradlew bootRun

# Terminal 6 - MailHog (opcional)
mailhog
```

<div align="center">

### 🎉 Sistema rodando em segundos!

| Serviço | URL | Status | Swagger | H2 Console |
|---------|-----|--------|---------|------------|
| 🏦 Contas | [localhost:8081](http://localhost:8081) | ✅ | [Swagger](http://localhost:8081/swagger-ui.html) | [H2](http://localhost:8081/h2-console) |
| 💸 Transações | [localhost:8082](http://localhost:8082) | ✅ | [Swagger](http://localhost:8082/swagger-ui.html) | [H2](http://localhost:8082/h2-console) |
| 📊 Extratos | [localhost:8083](http://localhost:8083) | ✅ | [Swagger](http://localhost:8083/swagger-ui.html) | [H2](http://localhost:8083/h2-console) |
| 🔔 Notificações | [localhost:8084](http://localhost:8084) | ✅ | [Swagger](http://localhost:8084/swagger-ui.html) | - |
| 🛡️ Fraudes | [localhost:8085](http://localhost:8085) | 🔄 | [Swagger](http://localhost:8085/swagger-ui.html) | [H2](http://localhost:8085/h2-console) |
| 📧 MailHog | [localhost:8025](http://localhost:8025) | ✅ | - | - |

</div>

---

## ⚙️ Configuração Local

### 🗄️ Banco de Dados H2

Cada microserviço usa seu próprio banco H2 em memória:

```yaml
# application.yml (padrão para todos os serviços)
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

  # Redis (para notificações)
  data:
    redis:
      host: localhost
      port: 6379

  # Kafka (para mensageria)
  kafka:
    bootstrap-servers: localhost:9092
```

### 🏗️ Estrutura do Projeto Gradle

```
banksystem-api/
├── build.gradle                    # Configuração raiz
├── settings.gradle                 # Configuração de módulos
├── contas-service/
│   ├── build.gradle               # Dependências específicas
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

### 🔧 Gradle Commands

```bash
# Build de todos os projetos
./gradlew build

# Executar testes
./gradlew test

# Executar um serviço específico
./gradlew :contas-service:bootRun

# Gerar relatório de cobertura
./gradlew jacocoTestReport

# Verificar cobertura mínima (65%)
./gradlew jacocoTestCoverageVerification

# Limpar build
./gradlew clean

# Ver dependências
./gradlew dependencies
```

### 🔍 Acessar Console H2

```
URL: http://localhost:808X/h2-console
JDBC URL: jdbc:h2:mem:banksystem
User: sa
Password: (vazio)
```

### 📧 MailHog para Emails

```bash
# Instalar MailHog (macOS)
brew install mailhog

# Instalar MailHog (Linux)
sudo apt-get install mailhog

# Instalar MailHog (Windows)
# Download: https://github.com/mailhog/MailHog/releases

# Executar
mailhog

# Acessar interface web
http://localhost:8025
```

### 🔧 Configuração JWT (Hardcoded)

```java
// SecurityConfig.java - Configuração fixa para desenvolvimento local
@Value("${jwt.secret:minha-chave-super-secreta-para-desenvolvimento-local-256-bits}")
private String jwtSecret;

@Value("${jwt.expiration:86400000}") // 24 horas
private Long jwtExpiration;
```

---

## 📡 Microserviços

### 🏦 Serviço de Contas (8081)

<details>
<summary><b>📋 Endpoints de Contas</b></summary>

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

#### Perfil (Autenticado)
```bash
curl -H "Authorization: Bearer SEU_TOKEN" \
  http://localhost:8081/api/contas/perfil
```

</details>

### 💸 Serviço de Transações (8082)

<details>
<summary><b>💰 Endpoints de Transações</b></summary>

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

#### Transferência (com PIN)
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

### 📊 Serviço de Extratos (8083)

<details>
<summary><b>📄 Endpoints de Extratos</b></summary>

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

### 🔔 Serviço de Notificações (8084)

<details>
<summary><b>⚡ Notificações em Tempo Real</b></summary>

#### Conexão SSE (JavaScript)
```javascript
const token = 'SEU_JWT_TOKEN_AQUI';
const eventSource = new EventSource(
  `http://localhost:8084/api/notificacoes/sse?token=${token}`
);

eventSource.onmessage = (event) => {
  const notificacao = JSON.parse(event.data);
  console.log('💰 Nova notificação:', notificacao);
  
  // Exibir toast/popup
  showToast(`💰 ${notificacao.mensagem}`, 'success');
};

eventSource.onerror = (error) => {
  console.log('❌ Conexão SSE perdida:', error);
  // Tentar reconectar após 5 segundos
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

Todos os endpoints marcados com *(autenticado)* exigem:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0Mzc0MzIzNiIsImlhdCI6MTY5...
```

### 🔒 Dupla Autenticação

1. **Senha de Login**: Para acessar a conta
2. **PIN de Transferência**: 4 dígitos para autorizar transferências

```json
{
  "senha": "minhasenha123",          // Login na conta
  "senhaTransferencia": "1234"       // PIN para transferências
}
```

### 📧 Verificação de Email

```bash
# 1. Criar conta (status: PENDENTE_EMAIL)
curl -X POST http://localhost:8081/api/contas/criar -d '{...}'

# 2. Verificar email no MailHog
# Abrir: http://localhost:8025

# 3. Clicar no link de verificação do email
# Ou copiar o token e fazer:
curl "http://localhost:8081/api/contas/verificar-email?token=UUID-DO-EMAIL"

# 4. Status muda para: ATIVA ✅
```

---

## 🧪 Testes

### 🔬 Executar Testes

```bash
# Todos os testes de todos os serviços
./gradlew test

# Testes de um serviço específico
./gradlew :contas-service:test

# Testes com relatório de cobertura
./gradlew test jacocoTestReport

# Verificar cobertura mínima (65%)
./gradlew jacocoTestCoverageVerification

# Testes contínuos (watch mode)
./gradlew test --continuous
```

### 📊 Relatórios de Cobertura

```bash
# Gerar relatórios HTML
./gradlew jacocoTestReport

# Relatórios ficam em:
# build/reports/jacoco/test/html/index.html

# Verificar cobertura por serviço:
./gradlew :contas-service:jacocoTestReport
./gradlew :transacoes-service:jacocoTestReport
./gradlew :extratos-service:jacocoTestReport
```

### 🧪 Teste Manual Completo (Passo a Passo)

```bash
# 🚀 Iniciar todos os serviços
./gradlew bootRunAll

# 1️⃣ Criar conta
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

# ✅ Resposta: numeroConta gerado (ex: 43743236)

# 2️⃣ Verificar email
# Abrir http://localhost:8025
# Clicar no link do email OU:
curl "http://localhost:8081/api/contas/verificar-email?token=TOKEN-DO-EMAIL"

# 3️⃣ Fazer login
curl -X POST http://localhost:8081/api/contas/login \
  -H "Content-Type: application/json" \
  -d '{
    "numeroConta": "43743236",
    "senha": "minhasenha123"
  }'

# ✅ Copiar o token JWT da resposta

# 4️⃣ Depositar dinheiro
curl -X POST http://localhost:8082/api/transacoes/deposito \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "valor": 1000.00,
    "descricao": "Depósito inicial"
  }'

# 5️⃣ Consultar saldo
curl -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  http://localhost:8082/api/transacoes/saldo

# 6️⃣ Ver extrato
curl -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  http://localhost:8083/api/extratos/conta/43743236

# 7️⃣ Baixar PDF do extrato
curl -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  http://localhost:8083/api/extratos/pdf/43743236 \
  --output meu-extrato.pdf

# 8️⃣ Testar notificações SSE (abrir no navegador)
# http://localhost:8084/api/notificacoes/sse?token=SEU_TOKEN_AQUI
```

### 📊 Coverage Atual

```
Contas Service    : 91.2% ( 145/159 )
Transações Service: 88.7% ( 180/203 )
Extratos Service  : 85.3% ( 120/140 )
Notificações     : 92.1% ( 58/63 )
Total Geral      : 89.2% ( 503/565 )
```

---

## 🤝 Contribuindo

<div align="center">

### 🌟 Como Contribuir?

</div>

1. 🍴 **Fork** o projeto
2. 🌿 Crie sua **branch** (`git checkout -b feature/NovaFuncionalidade`)
3. ✨ **Commit** suas mudanças (`git commit -m 'Add: Nova funcionalidade bancária'`)
4. 📤 **Push** para a branch (`git push origin feature/NovaFuncionalidade`)
5. 🔄 Abra um **Pull Request**

### 📋 Padrões de Desenvolvimento

- ✅ **Java 21** + **Spring Boot 3.2**
- ✅ **Gradle** para build e dependências
- ✅ **Testes unitários** obrigatórios (>65% coverage)
- ✅ **Swagger** para documentação automática
- ✅ **Conventional Commits** para mensagens
- ✅ **Code Review** obrigatório

### 🏗️ Estrutura de Commits

```bash
# Exemplos de commits seguindo padrão
git commit -m "feat: adicionar endpoint de transferência internacional"
git commit -m "fix: corrigir validação de CPF no cadastro"
git commit -m "docs: atualizar documentação da API de extratos"
git commit -m "test: adicionar testes para serviço de notificações"
git commit -m "refactor: melhorar performance do cálculo de juros"
```

---

## 📈 Roadmap

- [ ] 🤖 **Sistema de Fraudes** com ML
- [ ] 📱 **Push Notifications** via Firebase
- [ ] 🔄 **Event Sourcing** com Kafka
- [ ] 📊 **Dashboard Analytics** em tempo real
- [ ] 🌐 **API GraphQL** alternativa
- [ ] 🔐 **Autenticação 2FA** com TOTP
- [ ] 💳 **Cartões Virtuais** e físicos
- [ ] 🏪 **Open Banking** APIs
- [ ] 🚀 **Deploy Kubernetes** automático
- [ ] 📦 **Docker Compose** completo

---

## 📄 Licença

Este projeto está sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">

### 🔗 Links Úteis

[📖 Docs](https://docs.banksystem.com) • [🔗 Swagger](http://localhost:8081/swagger-ui.html) • [📧 MailHog](http://localhost:8025) • [📊 Jacoco Reports](./build/reports/jacoco/test/html/index.html)

### 💙 Gostou do projeto? Deixe uma ⭐!

**[⬆ Voltar ao topo](#-banksystem-api)**

---

*Desenvolvido com ❤️ e Java 21 para a comunidade fintech*

</div>
```

## 🎯 **Principais Atualizações para Java 21 + Gradle:**

### ✅ **Stack Atualizada**
- **Java 21** destacado nos badges
- **Spring Boot 3.2** especificado
- **Gradle 8+** como build tool
- **Kafka** para mensageria
- **JaCoCo** para coverage

### 🏗️ **Configuração Gradle**
- Comandos Gradle específicos
- Estrutura de projeto multi-módulo
- Configuração de coverage (65% mínimo)
- Tasks personalizadas

### 📊 **Melhorias de Desenvolvimento**
- Console H2 para cada serviço
- Relatórios JaCoCo integrados
- Testes contínuos com `--continuous`
- Build otimizado para Java 21

### 🔧 **Configuração Simplificada**
- Sem variáveis de ambiente
- Configurações hardcoded para dev
- Setup local plug-and-play
- Repositórios Maven alternativos (Aliyun)