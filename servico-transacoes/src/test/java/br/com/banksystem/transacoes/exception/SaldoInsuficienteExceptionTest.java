package br.com.banksystem.transacoes.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Testes da SaldoInsuficienteException")
class SaldoInsuficienteExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Given
        String mensagem = "Saldo insuficiente para realizar a transação";

        // When
        SaldoInsuficienteException exception = new SaldoInsuficienteException(mensagem);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Given
        SaldoInsuficienteException exception = new SaldoInsuficienteException("Saldo insuficiente");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção corretamente")
    void deveLancarExcecaoCorretamente() {
        // Given
        String mensagem = "Saldo disponível é menor que o valor solicitado";

        // When / Then
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> { throw new SaldoInsuficienteException(mensagem); }
        );

        assertThat(exception.getMessage()).isEqualTo(mensagem);
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // When
        SaldoInsuficienteException exception = new SaldoInsuficienteException(null);

        // Then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // When
        SaldoInsuficienteException exception = new SaldoInsuficienteException("");

        // Then
        assertThat(exception.getMessage()).isEmpty();
    }
}