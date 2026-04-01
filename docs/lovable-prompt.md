# Prompt para Geração do Frontend BankSystem no Lovable

## Contexto
Gere o frontend completo de um sistema bancário digital chamado **BankSystem**.
O backend já está desenvolvido em Spring Boot com múltiplos microserviços REST.

---

## Stack e Tecnologias

- **React 18** com TypeScript
- **Tailwind CSS** para estilização
- **shadcn/ui** como biblioteca de componentes
- **React Router v6** para navegação
- **Axios** para chamadas HTTP
- **React Hook Form** com Zod para validação de formulários
- **React Query (TanStack Query)** para gerenciamento de estado assíncrono
- **Lucide React** para ícones
- **Sonner** para notificações/toasts
- Tema: **dark mode** com cores primárias azul (#1a56db) e verde (#059669)

---

## Estrutura de APIs Backend

### Base URLs
```
Contas:       http://localhost:8081
Transações:   http://localhost:8082
Extratos:     http://localhost:8083
Notificações: http://localhost:8084 (SSE)
```

### Autenticação
Todas as requisições autenticadas usam cabeçalho:
```
Authorization: Bearer <jwt_token>
```
O token é retornado no login e deve ser salvo no `localStorage`.

---

## Páginas e Funcionalidades

### 1. Página de Cadastro (`/cadastro`)
Formulário com os campos:
- Nome Completo (texto)
- CPF (formato: 000.000.000-00, enviar sem máscara: 11 dígitos)
- E-mail
- Telefone (formato: (00) 00000-0000, enviar sem máscara: 11 dígitos)
- Senha (mínimo 6 caracteres, campo tipo password com toggle mostrar/ocultar)
- Senha de Transferência (exatamente 4 dígitos numéricos — explicar que é o PIN para autorizar transferências)
- Confirmar senha de transferência

**POST http://localhost:8081/api/contas/criar**
```json
{
  "nomeCompleto": "João Silva",
  "cpf": "12345678901",
  "email": "joao@email.com",
  "telefone": "11987654321",
  "senha": "senha123",
  "senhaTransferencia": "1234"
}
```

Após cadastro com sucesso, exibir tela/modal informando:
> "Conta criada com sucesso! Verifique seu e-mail para ativar sua conta antes de fazer login."
> Mostrar o número da conta gerado automaticamente em destaque.
> Botão "Reenviar e-mail de verificação" que chama:
> POST http://localhost:8081/api/contas/reenviar-verificacao?email={email}

---

### 2. Página de Login (`/login` — rota padrão)
Campos:
- Número da conta (8 dígitos, campo numérico)
- Senha (password com toggle)
- Botão "Entrar"
- Link "Criar conta"

**POST http://localhost:8081/api/contas/login**
```json
{ "numeroConta": "00123456", "senha": "senha123" }
```

Resposta de sucesso:
```json
{
  "sucesso": true,
  "dados": {
    "token": "eyJ...",
    "tipo": "Bearer",
    "numeroConta": "00123456",
    "nomeCompleto": "João Silva",
    "expiracaoMs": 86400000
  }
}
```

Salvar no localStorage: `token`, `numeroConta`, `nomeCompleto`.
Redirecionar para `/dashboard`.

---

### 3. Dashboard Principal (`/dashboard`) — ROTA PROTEGIDA

Layout com:
- **Sidebar lateral** (desktop) / **Bottom navigation** (mobile) com ícones para:
  - Dashboard (Home)
  - Transferir
  - Extrato
  - Perfil

**Header** com:
- Saudação "Olá, {nomeCompleto}!"
- Ícone de sino de notificações (badge com contador)
- Avatar com inicial do nome

**Cards principais:**
1. **Card Saldo** — destaque grande com:
   - "Saldo Disponível" e valor em R$ (com botão de olho para ocultar/mostrar)
   - "Limite Disponível" e valor em R$
   - GET http://localhost:8082/api/transacoes/saldo

2. **Ações Rápidas** — 4 botões grandes:
   - 💸 Transferir → abre modal/navega para /transferir
   - ➕ Depositar → abre modal para simular depósito (débito com valor positivo)
   - 📄 Extrato → navega para /extrato
   - 👤 Perfil → navega para /perfil

3. **Últimas Transações** — lista das 5 últimas:
   - GET http://localhost:8083/api/extratos/conta/{numeroConta}
   - Cada item: ícone (↑ saída vermelho / ↓ entrada verde), descrição, valor, data

---

### 4. Página de Transferência (`/transferir`) — ROTA PROTEGIDA

Formulário em 2 etapas:

**Etapa 1 — Dados da transferência:**
- Campo "Conta de destino" (8 dígitos) com botão "Buscar"
  - GET http://localhost:8081/api/contas/buscar/{numeroConta}
  - Exibir nome do titular encontrado como confirmação visual
- Campo "Valor" (R$, formato monetário)
- Campo "Descrição" (opcional)
- Botão "Continuar"

**Etapa 2 — Confirmar com PIN:**
- Resumo da transferência (de, para, nome destinatário, valor)
- Campo "Senha de Transferência" (4 dígitos numéricos, input type="password" com máximo 4 chars)
  - Exibir como 4 bolinhas/quadradinhos separados (estilo bancário)
- Botão "Confirmar Transferência"
- Botão "Cancelar"

**POST http://localhost:8082/api/transacoes/transferencia**
```json
{
  "contaDestino": "00654321",
  "valor": 150.00,
  "senhaTransferencia": "1234",
  "descricao": "Pagamento aluguel"
}
```

Sucesso: exibir tela de confirmação com comprovante animado (ícone de check animado + dados da transferência).

---

### 5. Página de Extrato (`/extrato`) — ROTA PROTEGIDA

Filtros:
- Seletor de período: "Últimos 7 dias", "Últimos 30 dias", "Personalizado" (date range picker)
- Filtro por tipo: Todos, Débito, Crédito, Transferências

Lista de transações com:
- Data e hora formatada em pt-BR
- Tipo com badge colorido (DEBITO = vermelho, CREDITO = azul, TRANSFERENCIA_SAIDA = laranja, TRANSFERENCIA_ENTRADA = verde)
- Descrição
- Valor formatado em R$
- Status (APROVADA = verde, REPROVADA = vermelho)

Botão "Baixar PDF":
- GET http://localhost:8083/api/extratos/pdf/{numeroConta}
- Fazer download do arquivo retornado como blob

Paginação se houver muitas transações.

**GETs:**
- GET http://localhost:8083/api/extratos/conta/{numeroConta}
- GET http://localhost:8083/api/extratos/periodo?numeroConta={n}&inicio={ISO}&fim={ISO}

---

### 6. Página de Perfil (`/perfil`) — ROTA PROTEGIDA

Exibir dados da conta:
- Nome, CPF (mascarado: ***.***.***-**), e-mail, telefone, número da conta
- Badge "E-mail Verificado ✅" ou "E-mail Não Verificado ⚠️"
- Data de criação da conta

Formulário de atualização:
- Nome, e-mail, telefone, nova senha (opcionais)
- PUT http://localhost:8081/api/contas/atualizar

Seção "Segurança" — Atualizar PIN de Transferência:
- Campo "PIN atual" (4 dígitos)
- Campo "Novo PIN" (4 dígitos)
- PUT http://localhost:8081/api/contas/senha-transferencia
- Body: `{ "senhaAtual": "1234", "novaSenha": "5678" }`

Botão "Sair" (logout) que limpa localStorage e redireciona para /login.

---

### 7. Notificações em Tempo Real (SSE)

Conectar ao SSE após login bem-sucedido:
```javascript
const token = localStorage.getItem('token');
const eventSource = new EventSource(
  `http://localhost:8084/api/notificacoes/sse?token=${token}`
);

eventSource.onmessage = (event) => {
  const notificacao = JSON.parse(event.data);
  // Exibir toast/push notification
  // tipos: TRANSACAO_APROVADA (verde), TRANSACAO_REPROVADA (vermelho)
};
```

Ao receber notificação:
- Exibir toast no canto superior direito com ícone e mensagem
- Incrementar badge de notificações no header
- Salvar na lista de notificações (estado local)

Painel de notificações (drawer/popover no sino):
- Lista das últimas notificações recebidas na sessão
- Marcar como lida ao clicar
- Botão "Limpar todas"

---

## Gerenciamento de Estado e Autenticação

```typescript
// src/contexts/AuthContext.tsx
interface AuthContextType {
  token: string | null;
  numeroConta: string | null;
  nomeCompleto: string | null;
  login: (token: string, numeroConta: string, nomeCompleto: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}
```

**Proteção de rotas:** Criar componente `RotaProtegida` que redireciona para `/login` se não autenticado.

---

## Configuração de APIs (Axios)

```typescript
// src/lib/api.ts
import axios from 'axios';

export const apiContas = axios.create({ baseURL: 'http://localhost:8081' });
export const apiTransacoes = axios.create({ baseURL: 'http://localhost:8082' });
export const apiExtratos = axios.create({ baseURL: 'http://localhost:8083' });

// Interceptor JWT
[apiContas, apiTransacoes, apiExtratos].forEach(api => {
  api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });
  api.interceptors.response.use(
    res => res,
    err => {
      if (err.response?.status === 401) {
        localStorage.clear();
        window.location.href = '/login';
      }
      return Promise.reject(err);
    }
  );
});
```

---

## Formato Padrão das Respostas da API

Todas as APIs retornam:
```json
{
  "sucesso": true,
  "dados": { ... },
  "mensagem": "Descrição",
  "timestamp": "2024-01-01T10:00:00"
}
```

Erro de validação (400):
```json
{
  "sucesso": false,
  "dados": { "campo": "mensagem de erro" },
  "mensagem": "Erro de validação nos campos informados"
}
```

Sempre verificar `response.data.sucesso` antes de usar `response.data.dados`.

---

## Design System

**Paleta de cores:**
- Primário: `#1a56db` (azul bancário)
- Sucesso/Entrada: `#059669` (verde)
- Perigo/Saída: `#dc2626` (vermelho)
- Aviso: `#d97706` (laranja)
- Fundo dark: `#0f172a`
- Card dark: `#1e293b`
- Texto: `#f1f5f9`

**Tipografia:** Inter (Google Fonts)

**Componentes customizados a criar:**
- `PinInput` — input de 4 dígitos estilo bancário (quadradinhos separados)
- `ValorInput` — input monetário com máscara R$
- `ContaInput` — input de 8 dígitos com formatação
- `SaldoCard` — card com toggle mostrar/ocultar valor
- `TransacaoItem` — item de lista de transação com ícone e cores
- `LoadingOverlay` — overlay de carregamento para operações financeiras

---

## Comportamentos Importantes

1. **Feedback visual:** Todas as operações financeiras devem mostrar loading state e resultado claro (sucesso/erro)
2. **Confirmação de transferência:** Sempre mostrar tela de confirmação antes de executar
3. **Máscaras de entrada:** CPF, telefone, valor monetário devem ter máscara visual
4. **Responsividade:** Layout adaptado para mobile (sidebar vira bottom nav)
5. **Segurança visual:** Saldo ocultável por padrão, PIN de 4 dígitos como input separado
6. **Erros do backend:** Exibir mensagem do campo `mensagem` da resposta de erro
7. **Token expirado:** Redirecionar para login automaticamente no interceptor Axios

---

## Estrutura de Arquivos Sugerida

```
src/
├── components/
│   ├── ui/           (shadcn components)
│   ├── layout/       (Sidebar, Header, BottomNav)
│   ├── bank/         (PinInput, SaldoCard, TransacaoItem, etc.)
│   └── shared/       (RotaProtegida, LoadingOverlay)
├── contexts/
│   ├── AuthContext.tsx
│   └── NotificacaoContext.tsx
├── hooks/
│   ├── useAuth.ts
│   ├── useSaldo.ts
│   ├── useExtrato.ts
│   └── useSSE.ts
├── lib/
│   ├── api.ts
│   └── utils.ts      (formatadores de moeda, data, CPF)
├── pages/
│   ├── Login.tsx
│   ├── Cadastro.tsx
│   ├── Dashboard.tsx
│   ├── Transferir.tsx
│   ├── Extrato.tsx
│   └── Perfil.tsx
└── types/
    └── api.types.ts
```

---

## Observações Finais

- O PIN de 4 dígitos (**senhaTransferencia**) é **diferente** da senha de login. Deixar isso claro na UI com texto explicativo.
- Ao fazer cadastro, a conta fica **inativa** até o e-mail ser verificado. Mostrar aviso claro na tela.
- O MailHog roda em **http://localhost:8025** — mencionar isso apenas em modo de desenvolvimento/debug.
- Usar **React Query** para cache e refetch automático do saldo após transações.
- O SSE deve ser reconectado automaticamente em caso de queda de conexão.
- Ao baixar PDF, usar `responseType: 'blob'` no Axios e criar URL temporária com `URL.createObjectURL`.
