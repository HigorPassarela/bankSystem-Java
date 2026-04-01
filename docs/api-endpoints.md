# BankSystem — Documentação de APIs

## Base URLs

| Serviço | URL Base |
|---|---|
| Contas | http://localhost:8081 |
| Transações | http://localhost:8082 |
| Extratos | http://localhost:8083 |
| Notificações | http://localhost:8084 |
| Fraudes | http://localhost:8085 |

---

## Autenticação

Todos os endpoints protegidos requerem o cabeçalho:
```
Authorization: Bearer <token_jwt>
```

---

## Serviço de Contas (porta 8081)

### POST /api/contas/criar
Cria uma nova conta bancária.

**Body:**
```json
{
  "nomeCompleto": "João Silva",
  "cpf": "12345678901",
  "email": "joao@email.com",
  "telefone": "11987654321",
  "senha": "minhasenha123"
}
```

**Resposta 201:**
```json
{
  "sucesso": true,
  "dados": {
    "numeroConta": "00123456",
    "nomeCompleto": "João Silva",
    "cpf": "12345678901",
    "email": "joao@email.com",
    "telefone": "11987654321",
    "ativa": true,
    "dataCriacao": "2024-01-01T10:00:00"
  },
  "mensagem": "Conta criada com sucesso",
  "timestamp": "2024-01-01T10:00:00"
}
```

---

### POST /api/contas/login
Autentica e retorna JWT.

**Body:**
```json
{
  "numeroConta": "00123456",
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
    "numeroConta": "00123456",
    "nomeCompleto": "João Silva",
    "expiracaoMs": 86400000
  },
  "mensagem": "Login realizado com sucesso",
  "timestamp": "2024-01-01T10:00:00"
}
```

---

### GET /api/contas/perfil *(autenticado)*
Retorna perfil da conta logada.

**cURL:**
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/contas/perfil
```

---

### PUT /api/contas/atualizar *(autenticado)*
Atualiza dados da conta.

**Body (campos opcionais):**
```json
{
  "nomeCompleto": "João Silva Atualizado",
  "email": "novo@email.com",
  "telefone": "11999999999",
  "novaSenha": "novasenha456"
}
```

---

## Serviço de Transações (porta 8082)

### POST /api/transacoes/debito *(autenticado)*
Realiza débito no saldo da conta.

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
    "numeroConta": "00123456",
    "valor": 150.00,
    "tipo": "DEBITO",
    "status": "APROVADA",
    "saldoAtualizado": 850.00,
    "dataHora": "2024-01-01T10:00:00"
  },
  "mensagem": "Débito processado com sucesso"
}
```

---

### POST /api/transacoes/credito *(autenticado)*
Realiza crédito usando limite disponível.

**Body:**
```json
{
  "valor": 300.00,
  "descricao": "Compra parcelada"
}
```

---

### GET /api/transacoes/saldo *(autenticado)*
Consulta saldo e limite disponível.

**cURL:**
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8082/api/transacoes/saldo
```

**Resposta:**
```json
{
  "sucesso": true,
  "dados": {
    "numeroConta": "00123456",
    "saldoDisponivel": 850.00,
    "limiteDisponivel": 4700.00
  },
  "mensagem": "Saldo consultado com sucesso"
}
```

---

## Serviço de Extratos (porta 8083)

### GET /api/extratos/conta/{numeroConta} *(autenticado)*

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8083/api/extratos/conta/00123456
```

---

### GET /api/extratos/periodo *(autenticado)*

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8083/api/extratos/periodo?numeroConta=00123456&inicio=2024-01-01T00:00:00&fim=2024-01-31T23:59:59"
```

---

### GET /api/extratos/pdf/{numeroConta} *(autenticado)*

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8083/api/extratos/pdf/00123456 --output extrato.pdf
```

---

## Serviço de Notificações (porta 8084)

### GET /api/notificacoes/sse?token={jwt}
Conecta ao stream SSE de notificações em tempo real.

**JavaScript (frontend):**
```javascript
const token = localStorage.getItem('jwt');
const eventSource = new EventSource(`http://localhost:8084/api/notificacoes/sse?token=${token}`);

eventSource.onmessage = (event) => {
  const notificacao = JSON.parse(event.data);
  console.log('Nova notificação:', notificacao);
};
```

**Formato do evento:**
```json
{
  "tipo": "TRANSACAO_APROVADA",
  "mensagem": "Transação de DEBITO no valor de R$ 150,00 aprovada com sucesso",
  "dados": { ... },
  "timestamp": "2024-01-01T10:00:00"
}
```

---

## Swagger UI

Acesse a documentação interativa:
- Contas: http://localhost:8081/swagger-ui.html
- Transações: http://localhost:8082/swagger-ui.html
- Extratos: http://localhost:8083/swagger-ui.html
- Notificações: http://localhost:8084/swagger-ui.html
- Fraudes: http://localhost:8085/swagger-ui.html
