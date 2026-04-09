package br.com.banksystem.transacoes.exception;

import br.com.banksystem.transacoes.dto.RespostaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Testes do GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve tratar SaldoInsuficienteException com status 422")
    void deveTratarSaldoInsuficienteException() {
        // Given
        SaldoInsuficienteException exception = new SaldoInsuficienteException("Saldo insuficiente para realizar a transação");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarSaldoInsuficiente(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Saldo insuficiente para realizar a transação");
        assertThat(response.getBody().dados()).isNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar TransferenciaInvalidaException com status 400")
    void deveTratarTransferenciaInvalidaException() {
        // Given
        TransferenciaInvalidaException exception = new TransferenciaInvalidaException("Conta de origem e destino não podem ser iguais");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarTransferenciaInvalida(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Conta de origem e destino não podem ser iguais");
        assertThat(response.getBody().dados()).isNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar ContaInativaException com status 403")
    void deveTratarContaInativaException() {
        // Given
        ContaInativaException exception = new ContaInativaException("Conta inativa para transações");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarContaInativa(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Conta inativa para transações");
        assertThat(response.getBody().dados()).isNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar erro de validação com status 400")
    void deveTratarErroDeValidacao() {
        // Given
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        MethodParameter methodParameter = Mockito.mock(MethodParameter.class);

        FieldError fieldError1 = new FieldError("transacaoDTO", "valor", "O valor deve ser maior que zero");
        FieldError fieldError2 = new FieldError("transacaoDTO", "numeroConta", "Número da conta é obrigatório");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<RespostaDTO<Map<String, String>>> response = globalExceptionHandler.tratarValidacao(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro de validação");
        assertThat(response.getBody().dados()).containsEntry("valor", "O valor deve ser maior que zero");
        assertThat(response.getBody().dados()).containsEntry("numeroConta", "Número da conta é obrigatório");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar erro de validação com um único campo")
    void deveTratarErroDeValidacaoComUmCampo() {
        // Given
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        MethodParameter methodParameter = Mockito.mock(MethodParameter.class);

        FieldError fieldError = new FieldError("transacaoDTO", "senha", "Senha é obrigatória");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<RespostaDTO<Map<String, String>>> response = globalExceptionHandler.tratarValidacao(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro de validação");
        assertThat(response.getBody().dados()).hasSize(1);
        assertThat(response.getBody().dados()).containsEntry("senha", "Senha é obrigatória");
    }

    @Test
    @DisplayName("Deve tratar exceção genérica com status 500")
    void deveTratarExcecaoGenerica() {
        // Given
        Exception exception = new Exception("Erro inesperado");

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
    @DisplayName("Deve tratar RuntimeException com status 500")
    void deveTratarRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Falha inesperada");

        // When
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarGeral(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Erro interno no servidor");
        assertThat(response.getBody().dados()).isNull();
    }
}