package br.com.banksystem.transacoes.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Testes da ContaInativaException")
class ContaInativaExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Given
        String mensagem = "Conta inativa para transações";

        // When
        ContaInativaException exception = new ContaInativaException(mensagem);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(mensagem);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerUmaRuntimeException() {
        // Given
        ContaInativaException exception = new ContaInativaException("Conta inativa");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção corretamente")
    void deveLancarExcecaoCorretamente() {
        // Given
        String mensagem = "Conta bloqueada temporariamente";

        // When / Then
        ContaInativaException exception = assertThrows(
                ContaInativaException.class,
                () -> { throw new ContaInativaException(mensagem); }
        );

        assertThat(exception.getMessage()).isEqualTo(mensagem);
    }

    @Test
    @DisplayName("Deve aceitar mensagem nula")
    void deveAceitarMensagemNula() {
        // When
        ContaInativaException exception = new ContaInativaException(null);

        // Then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Deve aceitar mensagem vazia")
    void deveAceitarMensagemVazia() {
        // When
        ContaInativaException exception = new ContaInativaException("");

        // Then
        assertThat(exception.getMessage()).isEmpty();
    }
}