# Guia de Integração Frontend — BankSystem

## Visão Geral

O BankSystem expõe APIs REST com CORS habilitado para integração com frontends modernos (React, Vue, Angular).
A autenticação é baseada em JWT — após login, inclua o token em todas as requisições protegidas.

---

## Configuração Inicial (React/Axios)

```bash
npm install axios
```

```javascript
// src/api/axiosConfig.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081', // trocar por serviço
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;
```

---

## Fluxo de Autenticação

```javascript
// 1. Criar conta
const criarConta = async (dados) => {
  const resposta = await axios.post('http://localhost:8081/api/contas/criar', dados);
  return resposta.data.dados; // PerfilContaDTO
};

// 2. Login
const login = async (numeroConta, senha) => {
  const resposta = await axios.post('http://localhost:8081/api/contas/login', {
    numeroConta, senha
  });
  const { token, nomeCompleto } = resposta.data.dados;
  localStorage.setItem('token', token);
  localStorage.setItem('nomeCompleto', nomeCompleto);
  localStorage.setItem('numeroConta', numeroConta);
  return resposta.data.dados;
};

// 3. Logout
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('nomeCompleto');
};
```

---

## Realizando Transações

```javascript
const apiTransacoes = axios.create({ baseURL: 'http://localhost:8082' });
apiTransacoes.interceptors.request.use(config => {
  config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`;
  return config;
});

// Débito
const debitar = async (valor, descricao) => {
  const resp = await apiTransacoes.post('/api/transacoes/debito', { valor, descricao });
  return resp.data.dados;
};

// Crédito
const creditar = async (valor, descricao) => {
  const resp = await apiTransacoes.post('/api/transacoes/credito', { valor, descricao });
  return resp.data.dados;
};

// Saldo
const obterSaldo = async () => {
  const resp = await apiTransacoes.get('/api/transacoes/saldo');
  return resp.data.dados;
};
```

---

## Notificações em Tempo Real (SSE)

```javascript
// src/hooks/useNotificacoes.js
import { useEffect, useState } from 'react';

export function useNotificacoes() {
  const [notificacoes, setNotificacoes] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const eventSource = new EventSource(
      `http://localhost:8084/api/notificacoes/sse?token=${token}`
    );

    eventSource.onmessage = (event) => {
      const notificacao = JSON.parse(event.data);
      setNotificacoes(prev => [notificacao, ...prev]);
    };

    eventSource.onerror = () => eventSource.close();

    return () => eventSource.close();
  }, []);

  return notificacoes;
}
```

---

## Extrato e PDF

```javascript
const apiExtratos = axios.create({ baseURL: 'http://localhost:8083' });
apiExtratos.interceptors.request.use(config => {
  config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`;
  return config;
});

// Listar extrato
const obterExtrato = async (numeroConta) => {
  const resp = await apiExtratos.get(`/api/extratos/conta/${numeroConta}`);
  return resp.data.dados;
};

// Download PDF
const downloadPdf = async (numeroConta) => {
  const resp = await apiExtratos.get(`/api/extratos/pdf/${numeroConta}`, {
    responseType: 'blob'
  });
  const url = URL.createObjectURL(resp.data);
  const a = document.createElement('a');
  a.href = url;
  a.download = `extrato-${numeroConta}.pdf`;
  a.click();
};
```

---

## Tratamento de Erros Padrão

```javascript
const tratarErro = (error) => {
  if (error.response) {
    const { sucesso, mensagem } = error.response.data;
    if (error.response.status === 401) {
      logout();
      window.location.href = '/login';
    }
    return mensagem || 'Erro desconhecido';
  }
  return 'Erro de conexão com o servidor';
};
```

---

## Formato de Resposta Padrão

Todas as APIs retornam:
```json
{
  "sucesso": true | false,
  "dados": { ... } | null,
  "mensagem": "Descrição da operação",
  "timestamp": "2024-01-01T10:00:00"
}
```

Em caso de erro de validação (400):
```json
{
  "sucesso": false,
  "dados": {
    "campo1": "mensagem de erro",
    "campo2": "mensagem de erro"
  },
  "mensagem": "Erro de validação nos campos informados",
  "timestamp": "2024-01-01T10:00:00"
}
```
