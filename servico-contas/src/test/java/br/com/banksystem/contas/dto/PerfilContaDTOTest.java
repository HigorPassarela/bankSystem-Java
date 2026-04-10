package br.com.banksystem.contas.dto;

import br.com.banksystem.contas.model.enums.StatusConta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do PerfilContaDTO")
class PerfilContaDTOTest {

    @Test
    @DisplayName("Deve criar record e retornar valores corretamente")
    void deveCriarRecordCorretamente() {
        LocalDateTime dataCriacao = LocalDateTime.now();

        PerfilContaDTO dto = new PerfilContaDTO(
                "12345-6",
                "João da Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                StatusConta.ATIVA,
                true,
                true,
                dataCriacao
        );

        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.nomeCompleto()).isEqualTo("João da Silva");
        assertThat(dto.cpf()).isEqualTo("12345678901");
        assertThat(dto.email()).isEqualTo("joao@email.com");
        assertThat(dto.telefone()).isEqualTo("11999999999");
        assertThat(dto.status()).isEqualTo(StatusConta.ATIVA);
        assertThat(dto.ativa()).isTrue();
        assertThat(dto.emailVerificado()).isTrue();
        assertThat(dto.dataCriacao()).isEqualTo(dataCriacao);
    }
}