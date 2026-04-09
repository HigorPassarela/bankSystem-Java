package br.com.banksystem.contas.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TokenDTO")
class TokenDTOTest {

    @Test
    @DisplayName("Deve criar record e retornar valores corretamente")
    void deveCriarRecordCorretamente() {
        TokenDTO dto = new TokenDTO(
                "jwt-token",
                "Bearer",
                "12345-6",
                "João da Silva",
                3600000L
        );

        assertThat(dto.token()).isEqualTo("jwt-token");
        assertThat(dto.tipo()).isEqualTo("Bearer");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.nomeCompleto()).isEqualTo("João da Silva");
        assertThat(dto.expiracaoMs()).isEqualTo(3600000L);
    }
}