package br.com.banksystem.contas.mapper;

import br.com.banksystem.contas.dto.CriarContaDTO;
import br.com.banksystem.contas.dto.PerfilContaDTO;
import br.com.banksystem.contas.model.Conta;
import br.com.banksystem.contas.model.dto.StatusConta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContaMapperTest {

    private ContaMapper contaMapper;

    @BeforeEach
    void setUp() {
        contaMapper = new ContaMapper();
    }

    @Test
    void deveConverterCriarContaDTOParaEntidade() {
        CriarContaDTO dto = new CriarContaDTO(
                "João Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "senha123",
                "1234"
        );

        String senhaHash = "senhaHash";
        String senhaTransferenciaHash = "pinHash";
        String numeroConta = "12345678";

        Conta conta = contaMapper.paraEntidade(dto, senhaHash, senhaTransferenciaHash, numeroConta);

        assertNotNull(conta);
        assertEquals("12345678", conta.getNumeroConta());
        assertEquals("João Silva", conta.getNomeCompleto());
        assertEquals("12345678901", conta.getCpf());
        assertEquals("joao@email.com", conta.getEmail());
        assertEquals("11999999999", conta.getTelefone());
        assertEquals("senhaHash", conta.getSenhaHash());
        assertEquals("pinHash", conta.getSenhaTransferenciaHash());
        assertEquals("ROLE_USUARIO", conta.getRole());
        assertEquals(StatusConta.PENDENTE_EMAIL, conta.getStatus());
        assertFalse(conta.getEmailVerificado());
        assertNotNull(conta.getDataCriacao());
        assertNotNull(conta.getDataAtualizacao());
    }

    @Test
    void deveConverterEntidadeParaPerfilDTO() {
        Conta conta = new Conta();
        conta.setNumeroConta("12345678");
        conta.setNomeCompleto("João Silva");
        conta.setCpf("12345678901");
        conta.setEmail("joao@email.com");
        conta.setTelefone("11999999999");
        conta.setStatus(StatusConta.ATIVA);
        conta.setEmailVerificado(true);
        conta.setDataCriacao(LocalDateTime.now());

        PerfilContaDTO dto = contaMapper.paraPerfilDTO(conta);

        assertNotNull(dto);
        assertEquals("12345678", dto.numeroConta());
        assertEquals("João Silva", dto.nomeCompleto());
        assertEquals("12345678901", dto.cpf());
        assertEquals("joao@email.com", dto.email());
        assertEquals("11999999999", dto.telefone());
        assertEquals(StatusConta.ATIVA, dto.status());
        assertTrue(dto.emailVerificado());
        assertNotNull(dto.dataCriacao());
    }

    @Test
    void deveManterContaComoPendenteEmailAoCriarEntidade() {
        CriarContaDTO dto = new CriarContaDTO(
                "Maria Souza",
                "98765432100",
                "maria@email.com",
                "11988887777",
                "senha456",
                "5678"
        );

        Conta conta = contaMapper.paraEntidade(dto, "hash1", "hash2", "87654321");

        assertEquals(StatusConta.PENDENTE_EMAIL, conta.getStatus());
        assertFalse(conta.getEmailVerificado());
        assertEquals("ROLE_USUARIO", conta.getRole());
    }
}