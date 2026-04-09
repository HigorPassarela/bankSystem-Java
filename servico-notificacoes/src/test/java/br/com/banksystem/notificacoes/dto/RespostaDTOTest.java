package br.com.banksystem.notificacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do RespostaDTO")
class RespostaDTOTest {

    @Test
    @DisplayName("Deve criar resposta de sucesso")
    void deveCriarRespostaDeSucesso() {
        // When
        RespostaDTO<String> resposta = RespostaDTO.sucesso("dados", "Operação realizada");

        // Then
        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("dados");
        assertThat(resposta.mensagem()).isEqualTo("Operação realizada");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar resposta de erro")
    void deveCriarRespostaDeErro() {
        // When
        RespostaDTO<Void> resposta = RespostaDTO.erro("Erro interno");

        // Then
        assertThat(resposta.sucesso()).isFalse();
        assertThat(resposta.dados()).isNull();
        assertThat(resposta.mensagem()).isEqualTo("Erro interno");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar record manualmente")
    void deveCriarRecordManualmente() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        RespostaDTO<String> resposta = new RespostaDTO<>(true, "abc", "ok", timestamp);

        // Then
        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("abc");
        assertThat(resposta.mensagem()).isEqualTo("ok");
        assertThat(resposta.timestamp()).isEqualTo(timestamp);
    }
}