package br.com.banksystem.contas.model;

import br.com.banksystem.contas.model.enums.StatusConta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContaTest {

    private Conta conta;
    private LocalDateTime dataAtual;

    @BeforeEach
    void setUp() {
        conta = new Conta();
        dataAtual = LocalDateTime.now();
    }

    @Test
    void deveInstanciarContaVazia() {
        Conta novaConta = new Conta();

        assertNotNull(novaConta);
        assertNull(novaConta.getId());
        assertNull(novaConta.getNumeroConta());
        assertNull(novaConta.getNomeCompleto());
    }

    @Test
    void deveDefinirEObterIdCorretamente() {
        String id = "507f1f77bcf86cd799439011";

        conta.setId(id);

        assertEquals(id, conta.getId());
    }

    @Test
    void deveDefinirEObterNumeroContaCorretamente() {
        String numeroConta = "123456789";

        conta.setNumeroConta(numeroConta);

        assertEquals(numeroConta, conta.getNumeroConta());
    }

    @Test
    void deveDefinirEObterNomeCompletoCorretamente() {
        String nomeCompleto = "João Silva Santos";

        conta.setNomeCompleto(nomeCompleto);

        assertEquals(nomeCompleto, conta.getNomeCompleto());
    }

    @Test
    void deveDefinirEObterCpfCorretamente() {
        String cpf = "12345678901";

        conta.setCpf(cpf);

        assertEquals(cpf, conta.getCpf());
    }

    @Test
    void deveDefinirEObterEmailCorretamente() {
        String email = "joao@email.com";

        conta.setEmail(email);

        assertEquals(email, conta.getEmail());
    }

    @Test
    void deveDefinirEObterTelefoneCorretamente() {
        String telefone = "11987654321";

        conta.setTelefone(telefone);

        assertEquals(telefone, conta.getTelefone());
    }

    @Test
    void deveDefinirEObterSenhaHashCorretamente() {
        String senhaHash = "$2a$10$hashedPassword";

        conta.setSenhaHash(senhaHash);

        assertEquals(senhaHash, conta.getSenhaHash());
    }

    @Test
    void deveDefinirEObterSenhaTransferenciaHashCorretamente() {
        String senhaTransferenciaHash = "$2a$10$hashedTransferPassword";

        conta.setSenhaTransferenciaHash(senhaTransferenciaHash);

        assertEquals(senhaTransferenciaHash, conta.getSenhaTransferenciaHash());
    }

    @Test
    void deveDefinirEObterRoleCorretamente() {
        String role = "ROLE_USER";

        conta.setRole(role);

        assertEquals(role, conta.getRole());
    }

    @Test
    void deveDefinirStatusAtivaEAtualizarCampoAtiva() {
        conta.setStatus(StatusConta.ATIVA);

        assertEquals(StatusConta.ATIVA, conta.getStatus());
        assertTrue(conta.getAtiva());
    }

    @Test
    void deveDefinirStatusPendenteEmailEAtualizarCampoAtiva() {
        conta.setStatus(StatusConta.PENDENTE_EMAIL);

        assertEquals(StatusConta.PENDENTE_EMAIL, conta.getStatus());
        assertFalse(conta.getAtiva());
    }

    @Test
    void deveDefinirStatusEncerradaEAtualizarCampoAtiva() {
        conta.setStatus(StatusConta.ENCERRADA);

        assertEquals(StatusConta.ENCERRADA, conta.getStatus());
        assertFalse(conta.getAtiva());
    }

    @Test
    void deveDefinirEObterAtivaCorretamente() {
        conta.setAtiva(true);

        assertTrue(conta.getAtiva());

        conta.setAtiva(false);

        assertFalse(conta.getAtiva());
    }

    @Test
    void deveDefinirEObterEmailVerificadoCorretamente() {
        conta.setEmailVerificado(true);

        assertTrue(conta.getEmailVerificado());

        conta.setEmailVerificado(false);

        assertFalse(conta.getEmailVerificado());
    }

    @Test
    void deveDefinirEObterTokenVerificacaoEmailCorretamente() {
        String token = "abc123def456";

        conta.setTokenVerificacaoEmail(token);

        assertEquals(token, conta.getTokenVerificacaoEmail());
    }

    @Test
    void deveDefinirEObterTokenVerificacaoExpiracaoCorretamente() {
        LocalDateTime expiracao = dataAtual.plusHours(24);

        conta.setTokenVerificacaoExpiracao(expiracao);

        assertEquals(expiracao, conta.getTokenVerificacaoExpiracao());
    }

    @Test
    void deveDefinirEObterDataCriacaoCorretamente() {
        conta.setDataCriacao(dataAtual);

        assertEquals(dataAtual, conta.getDataCriacao());
    }

    @Test
    void deveDefinirEObterDataAtualizacaoCorretamente() {
        conta.setDataAtualizacao(dataAtual);

        assertEquals(dataAtual, conta.getDataAtualizacao());
    }

    @Test
    void deveManterConsistenciaEntreStatusEAtiva() {
        conta.setStatus(StatusConta.ATIVA);
        assertTrue(conta.getAtiva());

        conta.setStatus(StatusConta.PENDENTE_EMAIL);
        assertFalse(conta.getAtiva());

        conta.setStatus(StatusConta.ENCERRADA);
        assertFalse(conta.getAtiva());
    }

    @Test
    void devePermitirValoresNulosParaCamposOpcionais() {
        conta.setTokenVerificacaoEmail(null);
        conta.setTokenVerificacaoExpiracao(null);
        conta.setSenhaTransferenciaHash(null);

        assertNull(conta.getTokenVerificacaoEmail());
        assertNull(conta.getTokenVerificacaoExpiracao());
        assertNull(conta.getSenhaTransferenciaHash());
    }

    @Test
    void deveDefinirTodosOsCamposCorretamente() {
        String id = "507f1f77bcf86cd799439011";
        String numeroConta = "123456789";
        String nomeCompleto = "João Silva Santos";
        String cpf = "12345678901";
        String email = "joao@email.com";
        String telefone = "11987654321";
        String senhaHash = "$2a$10$hashedPassword";
        String senhaTransferenciaHash = "$2a$10$hashedTransferPassword";
        String role = "ROLE_USER";
        String token = "abc123def456";
        LocalDateTime expiracao = dataAtual.plusHours(24);

        conta.setId(id);
        conta.setNumeroConta(numeroConta);
        conta.setNomeCompleto(nomeCompleto);
        conta.setCpf(cpf);
        conta.setEmail(email);
        conta.setTelefone(telefone);
        conta.setSenhaHash(senhaHash);
        conta.setSenhaTransferenciaHash(senhaTransferenciaHash);
        conta.setRole(role);
        conta.setStatus(StatusConta.ATIVA);
        conta.setEmailVerificado(true);
        conta.setTokenVerificacaoEmail(token);
        conta.setTokenVerificacaoExpiracao(expiracao);
        conta.setDataCriacao(dataAtual);
        conta.setDataAtualizacao(dataAtual);

        assertEquals(id, conta.getId());
        assertEquals(numeroConta, conta.getNumeroConta());
        assertEquals(nomeCompleto, conta.getNomeCompleto());
        assertEquals(cpf, conta.getCpf());
        assertEquals(email, conta.getEmail());
        assertEquals(telefone, conta.getTelefone());
        assertEquals(senhaHash, conta.getSenhaHash());
        assertEquals(senhaTransferenciaHash, conta.getSenhaTransferenciaHash());
        assertEquals(role, conta.getRole());
        assertEquals(StatusConta.ATIVA, conta.getStatus());
        assertTrue(conta.getAtiva());
        assertTrue(conta.getEmailVerificado());
        assertEquals(token, conta.getTokenVerificacaoEmail());
        assertEquals(expiracao, conta.getTokenVerificacaoExpiracao());
        assertEquals(dataAtual, conta.getDataCriacao());
        assertEquals(dataAtual, conta.getDataAtualizacao());
    }

    @Test
    void devePermitirAlteracaoDeStatus() {
        conta.setStatus(StatusConta.PENDENTE_EMAIL);
        assertEquals(StatusConta.PENDENTE_EMAIL, conta.getStatus());
        assertFalse(conta.getAtiva());

        conta.setStatus(StatusConta.ATIVA);
        assertEquals(StatusConta.ATIVA, conta.getStatus());
        assertTrue(conta.getAtiva());

        conta.setStatus(StatusConta.ENCERRADA);
        assertEquals(StatusConta.ENCERRADA, conta.getStatus());
        assertFalse(conta.getAtiva());
    }
}