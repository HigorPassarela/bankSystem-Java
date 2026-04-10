package br.com.banksystem.transacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do RespostaDTO")
class RespostaDTOTest {

    @Test
    @DisplayName("Deve criar resposta de sucesso")
    void deveCriarRespostaDeSucesso() {
        RespostaDTO<String> resposta = RespostaDTO.sucesso("dados", "Sucesso");

        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isEqualTo("dados");
        assertThat(resposta.mensagem()).isEqualTo("Sucesso");
        assertThat(resposta.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar resposta de erro")
    void deveCriarRespostaDeErro() {
        RespostaDTO<Void> resposta = RespostaDTO.erro("Erro interno");

        assertThat(resposta.sucesso()).isFalse();
        assertThat(resposta.dados()).isNull();
        assertThat(resposta.mensagem()).isEqualTo("Erro interno");
        assertThat(resposta.timestamp()).isNotNull();
    }
}