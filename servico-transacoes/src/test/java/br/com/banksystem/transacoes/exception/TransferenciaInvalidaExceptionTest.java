package br.com.banksystem.transacoes.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Testes da TransferenciaInvalidaException")
class TransferenciaInvalidaExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Given
        String mensagem = "Transferência inválida";

        // When
        TransferenciaInvalidaException exception = new TransferenciaInvalidaException(mensagem);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Given
        TransferenciaInvalidaException exception = new TransferenciaInvalidaException("Conta de destino inválida");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção corretamente")
    void deveLancarExcecaoCorretamente() {
        // Given
        String mensagem = "Conta de origem e destino não podem ser iguais";

        // When / Then
        TransferenciaInvalidaException exception = assertThrows(
                TransferenciaInvalidaException.class,
                () -> { throw new TransferenciaInvalidaException(mensagem); }
        );

        assertThat(exception.getMessage()).isEqualTo(mensagem);
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // When
        TransferenciaInvalidaException exception = new TransferenciaInvalidaException(null);

        // Then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // When
        TransferenciaInvalidaException exception = new TransferenciaInvalidaException("");

        // Then
        assertThat(exception.getMessage()).isEmpty();
    }
}