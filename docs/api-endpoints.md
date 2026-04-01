# BankSystem — Documentação de APIs

## Base URLs

| Serviço | URL Base | Swagger |
|---|---|---|
| Contas | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Transações | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Extratos | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Notificações | http://localhost:8084 | http://localhost:8084/swagger-ui.html |
| Fraudes | http://localhost:8085 | http://localhost:8085/swagger-ui.html |

---

## Autenticação

Todos os endpoints marcados com *(autenticado)* exigem o cabeçalho:
```
Authorization: Bearer <token_jwt>
```

O token é obtido em `POST /api/contas/login` e expira em 24 horas.
No Swagger UI, clique em **Authorize 🔒** e cole `Bearer SEU_TOKEN`.

---

## Ciclo de vida da conta

```
PENDENTE_EMAIL → (verificação de e-mail) → ATIVA → SUSPENSA / ENCERRADA
```

Contas `PENDENTE_EMAIL` não conseguem fazer login. Apenas contas `ATIVA` realizam transações.

---

## Serviço de Contas (porta 8081)

### POST /api/contas/criar
Cria uma nova conta bancária. Status inicial: `PENDENTE_EMAIL`.
Envia e-mail de verificação via MailHog (http://localhost:8025).

**Body:**
```json
{
  "nomeCompleto": "João Silva",
  "cpf": "12345678901",
  "email": "joao@email.com",
  "telefone": "11987654321",
  "senha": "minhasenha123",
  "senhaTransferencia": "1234"
}
```

> `senhaTransferencia` é o PIN de **exatamente 4 dígitos** usado para autorizar transferências.
> É diferente da senha de login e armazenado separadamente com BCrypt.

**Resposta 201:**
```json
{
  "sucesso": true,
  "dados": {
    "numeroConta": "43743236",
    "nomeCompleto": "João Silva",
    "cpf": "12345678901",
    "email": "joao@email.com",
    "telefone": "11987654321",
    "status": "PENDENTE_EMAIL",
    "ativa": false,
    "emailVerificado": false,
    "dataCriacao": "2026-04-01T20:03:19.061"
  },
  "mensagem": "Conta criada! Verifique seu e-mail para ativar a conta.",
  "timestamp": "2026-04-01T20:03:19.455"
}
```

---

### GET /api/contas/verificar-email?token={token}
Ativa a conta após o usuário clicar no link do e-mail.
Status muda de `PENDENTE_EMAIL` → `ATIVA`.

```bash
curl "http://localhost:8081/api/contas/verificar-email?token=UUID-DO-TOKEN"
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": { "mensagem": "E-mail verificado com sucesso! Conta 43743236 está ATIVA.", "verificado": true },
  "mensagem": "E-mail verificado com sucesso! Conta 43743236 está ATIVA.",
  "timestamp": "2026-04-01T20:05:00"
}
```

---

### POST /api/contas/reenviar-verificacao?email={email}
Reenvia o e-mail de verificação para contas ainda `PENDENTE_EMAIL`.

```bash
curl -X POST "http://localhost:8081/api/contas/reenviar-verificacao?email=joao@email.com"
```

---

### POST /api/contas/login
Autentica a conta e retorna token JWT.
Requer conta com status `ATIVA`.

**Body:**
```json
{
  "numeroConta": "43743236",
  "senha": "minhasenha123"
}
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer",
    "numeroConta": "43743236",
    "nomeCompleto": "João Silva",
    "expiracaoMs": 86400000
  },
  "mensagem": "Login realizado com sucesso",
  "timestamp": "2026-04-01T20:05:30"
}
```

---

### GET /api/contas/perfil *(autenticado)*
Retorna os dados do perfil da conta autenticada, incluindo o status atual.

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/contas/perfil
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "numeroConta": "43743236",
    "nomeCompleto": "João Silva",
    "cpf": "12345678901",
    "email": "joao@email.com",
    "telefone": "11987654321",
    "status": "ATIVA",
    "ativa": true,
    "emailVerificado": true,
    "dataCriacao": "2026-04-01T20:03:19.061"
  },
  "mensagem": "Perfil obtido com sucesso"
}
```

---

### PUT /api/contas/atualizar *(autenticado)*
Atualiza dados da conta. Todos os campos são opcionais.

**Body:**
```json
{
  "nomeCompleto": "João Silva Atualizado",
  "email": "novo@email.com",
  "telefone": "11999999999",
  "novaSenha": "novasenha456"
}
```

---

### PUT /api/contas/senha-transferencia *(autenticado)*
Atualiza o PIN de 4 dígitos usado para transferências.

**Body:**
```json
{
  "senhaAtual": "1234",
  "novaSenha": "5678"
}
```

---

### GET /api/contas/buscar/{numeroConta} *(autenticado)*
Busca dados básicos de uma conta por número (usado para confirmar destinatário antes de transferir).

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/contas/buscar/12345678
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "numeroConta": "12345678",
    "nomeCompleto": "Maria Oliveira",
    "status": "ATIVA"
  },
  "mensagem": "Conta encontrada"
}
```

---

## Serviço de Transações (porta 8082)

### POST /api/transacoes/deposito *(autenticado)*
Deposita valor na conta — credita no saldo disponível.
Não exige PIN de transferência.

**Body:**
```json
{
  "valor": 500.00,
  "descricao": "Depósito inicial"
}
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "idTransacao": "a1b2c3d4-...",
    "numeroConta": "43743236",
    "valor": 500.00,
    "tipo": "DEPOSITO",
    "status": "APROVADA",
    "saldoAtualizado": 500.00,
    "dataHora": "2026-04-01T20:10:00"
  },
  "mensagem": "Depósito de R$ 500.00 realizado com sucesso"
}
```

---

### POST /api/transacoes/debito *(autenticado)*
Debita valor do saldo disponível. Falha se saldo for insuficiente.

**Body:**
```json
{
  "valor": 150.00,
  "descricao": "Compra no supermercado"
}
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "idTransacao": "uuid-aqui",
    "numeroConta": "43743236",
    "valor": 150.00,
    "tipo": "DEBITO",
    "status": "APROVADA",
    "saldoAtualizado": 350.00,
    "dataHora": "2026-04-01T20:11:00"
  },
  "mensagem": "Débito processado com sucesso"
}
```

**Resposta 422 (saldo insuficiente):**
```json
{
  "sucesso": false,
  "dados": null,
  "mensagem": "Saldo insuficiente. Disponível: R$ 350.00",
  "timestamp": "2026-04-01T20:11:00"
}
```

---

### POST /api/transacoes/credito *(autenticado)*
Usa o limite disponível (modalidade "pagar depois").

**Body:**
```json
{
  "valor": 300.00,
  "descricao": "Compra parcelada"
}
```

---

### POST /api/transacoes/transferencia *(autenticado)*
Transfere saldo entre contas. Exige PIN de 4 dígitos para autorizar.

**Body:**
```json
{
  "contaDestino": "12345678",
  "valor": 200.00,
  "senhaTransferencia": "1234",
  "descricao": "Pagamento aluguel"
}
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "idTransacao": "uuid-aqui",
    "contaOrigem": "43743236",
    "contaDestino": "12345678",
    "valor": 200.00,
    "status": "APROVADA",
    "saldoOrigemAtualizado": 150.00,
    "dataHora": "2026-04-01T20:12:00"
  },
  "mensagem": "Transferência realizada com sucesso"
}
```

**Resposta 400 (PIN inválido):**
```json
{
  "sucesso": false,
  "dados": null,
  "mensagem": "Senha de transferência (PIN) inválida",
  "timestamp": "2026-04-01T20:12:00"
}
```

---

### GET /api/transacoes/saldo *(autenticado)*
Consulta saldo e limite disponíveis.

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8082/api/transacoes/saldo
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": {
    "numeroConta": "43743236",
    "saldoDisponivel": 150.00,
    "limiteDisponivel": 5000.00
  },
  "mensagem": "Saldo consultado com sucesso"
}
```

---

### GET /api/transacoes/limite *(autenticado)*
Consulta limite de crédito disponível (mesma estrutura do `/saldo`).

---

## Serviço de Extratos (porta 8083)

### GET /api/extratos/conta/{numeroConta} *(autenticado)*
Lista todas as transações da conta, ordenadas da mais recente para a mais antiga.

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8083/api/extratos/conta/43743236
```

**Resposta 200:**
```json
{
  "sucesso": true,
  "dados": [
    {
      "idTransacao": "uuid-1",
      "numeroConta": "43743236",
      "valor": 200.00,
      "tipo": "TRANSFERENCIA_SAIDA",
      "status": "APROVADA",
      "descricao": "Transferência para conta 12345678 — Pagamento aluguel",
      "dataHora": "2026-04-01T20:12:00",
      "saldoAposTransacao": 150.00
    },
    {
      "idTransacao": "uuid-2",
      "numeroConta": "43743236",
      "valor": 150.00,
      "tipo": "DEBITO",
      "status": "APROVADA",
      "descricao": "Compra no supermercado",
      "dataHora": "2026-04-01T20:11:00",
      "saldoAposTransacao": 350.00
    },
    {
      "idTransacao": "uuid-3",
      "numeroConta": "43743236",
      "valor": 500.00,
      "tipo": "DEPOSITO",
      "status": "APROVADA",
      "descricao": "Depósito inicial",
      "dataHora": "2026-04-01T20:10:00",
      "saldoAposTransacao": 500.00
    }
  ],
  "mensagem": "3 transação(ões) encontrada(s)"
}
```

---

### GET /api/extratos/conta/{numeroConta}/paginado *(autenticado)*
Listagem paginada — útil para históricos longos.

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8083/api/extratos/conta/43743236/paginado?pagina=0&tamanho=10"
```

---

### GET /api/extratos/periodo *(autenticado)*
Filtra transações entre duas datas no formato ISO 8601.

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8083/api/extratos/periodo?numeroConta=43743236&inicio=2026-04-01T00:00:00&fim=2026-04-30T23:59:59"
```

---

### GET /api/extratos/tipo *(autenticado)*
Filtra transações por tipo.

Tipos disponíveis: `DEPOSITO` | `DEBITO` | `CREDITO` | `TRANSFERENCIA_SAIDA` | `TRANSFERENCIA_ENTRADA`

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8083/api/extratos/tipo?numeroConta=43743236&tipo=DEPOSITO"
```

---

### GET /api/extratos/pdf/{numeroConta} *(autenticado)*
Gera e faz download do PDF com o extrato completo da conta.

O PDF inclui: cabeçalho com dados da conta, tabela de transações com cores (entradas em verde,
saídas em vermelho), totalizadores de entradas/saídas/saldo do período e rodapé com data de geração.

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8083/api/extratos/pdf/43743236 --output extrato.pdf
```

---

### GET /api/extratos/pdf/{numeroConta}/periodo *(autenticado)*
Gera PDF filtrado por período. O nome do arquivo reflete o intervalo de datas.

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8083/api/extratos/pdf/43743236/periodo?inicio=2026-04-01T00:00:00&fim=2026-04-30T23:59:59" \
  --output extrato-abril.pdf
```

---

## Serviço de Notificações (porta 8084)

### GET /api/notificacoes/sse?token={jwt}
Abre uma conexão SSE (Server-Sent Events) para receber notificações em tempo real.
O token JWT é passado como query param (sem cabeçalho, pois SSE não suporta headers customizados).

**JavaScript (frontend):**
```javascript
const token = localStorage.getItem('token');
const eventSource = new EventSource(
  `http://localhost:8084/api/notificacoes/sse?token=${token}`
);

eventSource.onmessage = (event) => {
  const notificacao = JSON.parse(event.data);
  console.log('Nova notificação:', notificacao);
};

eventSource.onerror = () => eventSource.close();
```

**Formato do evento recebido:**
```json
{
  "tipo": "TRANSACAO_APROVADA",
  "mensagem": "Transação de DEPOSITO no valor de R$ 500,00 aprovada com sucesso",
  "dados": { "idTransacao": "...", "valor": 500.00, "tipo": "DEPOSITO" },
  "timestamp": "2026-04-01T20:10:00"
}
```

Tipos de notificação: `TRANSACAO_APROVADA` | `TRANSACAO_REPROVADA`

---

### GET /api/notificacoes/historico *(autenticado)*
Retorna o histórico de notificações da sessão atual.

---

## Formato padrão de resposta

Todas as APIs retornam o mesmo envelope:

```json
{
  "sucesso": true,
  "dados": { ... },
  "mensagem": "Descrição da operação",
  "timestamp": "2026-04-01T20:00:00"
}
```

**Erro de validação (400):**
```json
{
  "sucesso": false,
  "dados": {
    "valor": "Valor mínimo de depósito é R$ 0,01",
    "senhaTransferencia": "Senha de transferência deve ter exatamente 4 dígitos numéricos"
  },
  "mensagem": "Erro de validação nos campos informados",
  "timestamp": "2026-04-01T20:00:00"
}
```

**Saldo insuficiente (422):**
```json
{
  "sucesso": false,
  "dados": null,
  "mensagem": "Saldo insuficiente. Disponível: R$ 150.00",
  "timestamp": "2026-04-01T20:00:00"
}
```

---

## Resumo de todos os endpoints

| Método | Endpoint | Serviço | Auth | Descrição |
|---|---|---|---|---|
| POST | /api/contas/criar | Contas | ❌ | Criar conta (status: PENDENTE_EMAIL) |
| GET | /api/contas/verificar-email | Contas | ❌ | Ativar conta via link do e-mail |
| POST | /api/contas/reenviar-verificacao | Contas | ❌ | Reenviar e-mail de verificação |
| POST | /api/contas/login | Contas | ❌ | Login → retorna JWT |
| GET | /api/contas/perfil | Contas | ✅ | Perfil com status atual |
| PUT | /api/contas/atualizar | Contas | ✅ | Atualizar dados pessoais |
| PUT | /api/contas/senha-transferencia | Contas | ✅ | Atualizar PIN de 4 dígitos |
| GET | /api/contas/buscar/{numeroConta} | Contas | ✅ | Buscar dados de outra conta |
| POST | /api/transacoes/deposito | Transações | ✅ | Depositar saldo |
| POST | /api/transacoes/debito | Transações | ✅ | Débito no saldo |
| POST | /api/transacoes/credito | Transações | ✅ | Crédito no limite |
| POST | /api/transacoes/transferencia | Transações | ✅ | Transferência (exige PIN) |
| GET | /api/transacoes/saldo | Transações | ✅ | Saldo e limite disponíveis |
| GET | /api/transacoes/limite | Transações | ✅ | Limite de crédito disponível |
| GET | /api/extratos/conta/{numeroConta} | Extratos | ✅ | Histórico completo |
| GET | /api/extratos/conta/{numeroConta}/paginado | Extratos | ✅ | Histórico paginado |
| GET | /api/extratos/periodo | Extratos | ✅ | Extrato por período |
| GET | /api/extratos/tipo | Extratos | ✅ | Filtrar por tipo |
| GET | /api/extratos/pdf/{numeroConta} | Extratos | ✅ | Download PDF completo |
| GET | /api/extratos/pdf/{numeroConta}/periodo | Extratos | ✅ | Download PDF por período |
| GET | /api/notificacoes/sse | Notificações | token param | Stream SSE tempo real |
| GET | /api/notificacoes/historico | Notificações | ✅ | Histórico de notificações |