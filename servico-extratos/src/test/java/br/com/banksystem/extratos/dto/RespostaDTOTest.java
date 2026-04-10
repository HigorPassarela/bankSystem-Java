package br.com.banksystem.extratos.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do RespostaDTO")
class RespostaDTOTest {

    @Test
    @DisplayName("Deve criar resposta de sucesso corretamente")
    void deveCriarRespostaDeSucessoCorretamente() {
        // When
        RespostaDTO<String> resposta = RespostaDTO.sucesso("dados-teste", "Operação realizada com sucesso");

        // Then
        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("dados-teste");
        assertThat(resposta.mensagem()).isEqualTo("Operação realizada com sucesso");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar resposta de erro corretamente")
    void deveCriarRespostaDeErroCorretamente() {
        // When
        RespostaDTO<Void> resposta = RespostaDTO.erro("Erro ao processar requisição");

        // Then
        assertThat(resposta.sucesso()).isFalse();
        assertThat(resposta.dados()).isNull();
        assertThat(resposta.mensagem()).isEqualTo("Erro ao processar requisição");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar record manualmente")
    void deveCriarRecordManualmente() {
        // Given
        LocalDateTime agora = LocalDateTime.now();

        // When
        RespostaDTO<String> resposta = new RespostaDTO<>(true, "abc", "ok", agora);

        // Then
        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("abc");
        assertThat(resposta.mensagem()).isEqualTo("ok");
        assertThat(resposta.timestamp()).isEqualTo(agora);
    }
}