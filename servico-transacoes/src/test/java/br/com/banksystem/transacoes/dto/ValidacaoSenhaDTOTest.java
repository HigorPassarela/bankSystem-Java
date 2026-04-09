package br.com.banksystem.transacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do ValidacaoSenhaDTO")
class ValidacaoSenhaDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        Object dados = "dados-teste";
        Object timestamp = "2026-04-09T12:00:00";

        ValidacaoSenhaDTO dto = new ValidacaoSenhaDTO(
                true,
                dados,
                "Senha válida",
                timestamp
        );

        assertThat(dto.sucesso()).isTrue();
        assertThat(dto.dados()).isEqualTo(dados);
        assertThat(dto.mensagem()).isEqualTo("Senha válida");
        assertThat(dto.timestamp()).isEqualTo(timestamp);
    }
}