package br.com.banksystem.contas.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do RespostaDTO")
class RespostaDTOTest {

    @Test
    @DisplayName("Deve criar resposta de sucesso")
    void deveCriarRespostaDeSucesso() {
        RespostaDTO<String> resposta = RespostaDTO.sucesso("dados-teste", "Operação realizada com sucesso");

        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("dados-teste");
        assertThat(resposta.mensagem()).isEqualTo("Operação realizada com sucesso");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar resposta de erro")
    void deveCriarRespostaDeErro() {
        RespostaDTO<Void> resposta = RespostaDTO.erro("Erro ao processar solicitação");

        assertThat(resposta.sucesso()).isFalse();
        assertThat(resposta.dados()).isNull();
        assertThat(resposta.mensagem()).isEqualTo("Erro ao processar solicitação");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar record manualmente")
    void deveCriarRecordManualmente() {
        RespostaDTO<String> resposta = new RespostaDTO<>(true, "abc", "ok", java.time.LocalDateTime.now());

        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("abc");
        assertThat(resposta.mensagem()).isEqualTo("ok");
        assertThat(resposta.timestamp()).isNotNull();
    }
}