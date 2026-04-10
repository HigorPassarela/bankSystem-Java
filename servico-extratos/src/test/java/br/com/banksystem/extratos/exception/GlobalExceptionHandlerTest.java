package br.com.banksystem.extratos.exception;

import br.com.banksystem.extratos.dto.RespostaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve tratar exceção genérica e retornar status 500")
    void deveTratarExcecaoGenericaERetornarStatus500() {
        // Given
        Exception exception = new Exception("Erro inesperado");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarGeral(exception);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro interno no servidor");
        assertThat(response.getBody().dados()).isNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar RuntimeException e retornar status 500")
    void deveTratarRuntimeExceptionERetornarStatus500() {
        // Given
        RuntimeException exception = new RuntimeException("Falha em tempo de execução");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarGeral(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro interno no servidor");
        assertThat(response.getBody().dados()).isNull();
    }

    @Test
    @DisplayName("Deve tratar exceção com mensagem nula")
    void deveTratarExcecaoComMensagemNula() {
        // Given
        Exception exception = new Exception((String) null);

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarGeral(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro interno no servidor");
        assertThat(response.getBody().dados()).isNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar ResponseEntity com body preenchido corretamente")
    void deveRetornarResponseEntityComBodyPreenchidoCorretamente() {
        // Given
        Exception exception = new IllegalArgumentException("Argumento inválido");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarGeral(exception);

        // Then
        RespostaDTO<Void> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.sucesso()).isFalse();
        assertThat(body.dados()).isNull();
        assertThat(body.mensagem()).isEqualTo("Erro interno no servidor");
        assertThat(body.timestamp()).isNotNull();
    }
}