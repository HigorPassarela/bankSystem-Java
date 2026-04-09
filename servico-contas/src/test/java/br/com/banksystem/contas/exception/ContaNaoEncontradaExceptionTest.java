package br.com.banksystem.contas.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContaNaoEncontradaExceptionTest {

    @Test
    void construtor_ComMensagem_DeveDefinirMensagemCorretamente() {
        // Arrange
        String mensagemEsperada = "Conta com número 12345 não foi encontrada";

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemEsperada);

        // Assert
        assertEquals(mensagemEsperada, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void construtor_ComMensagemNula_DeveAceitarMensagemNula() {
        // Arrange
        String mensagemNula = null;

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemNula);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void construtor_ComMensagemVazia_DeveDefinirMensagemVazia() {
        // Arrange
        String mensagemVazia = "";

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemVazia);

        // Assert
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void construtor_ComMensagemECausa_DeveDefinirMensagemECausaCorretamente() {
        // Arrange
        String mensagemEsperada = "Erro ao buscar conta no banco de dados";
        Throwable causaEsperada = new RuntimeException("Conexão com banco falhou");

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemEsperada, causaEsperada);

        // Assert
        assertEquals(mensagemEsperada, exception.getMessage());
        assertEquals(causaEsperada, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void construtor_ComMensagemECausaNula_DeveAceitarCausaNula() {
        // Arrange
        String mensagemEsperada = "Conta não encontrada";
        Throwable causaNula = null;

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemEsperada, causaNula);

        // Assert
        assertEquals(mensagemEsperada, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void construtor_ComMensagemNulaECausa_DeveAceitarMensagemNulaComCausa() {
        // Arrange
        String mensagemNula = null;
        Throwable causaEsperada = new IllegalArgumentException("Parâmetro inválido");

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemNula, causaEsperada);

        // Assert
        assertNull(exception.getMessage());
        assertEquals(causaEsperada, exception.getCause());
    }

    @Test
    void construtor_ComCausaCompleta_DeveManterStackTrace() {
        // Arrange
        String mensagem = "Falha na consulta da conta";
        Exception causaOriginal = new Exception("Erro de SQL");
        causaOriginal.fillInStackTrace();

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagem, causaOriginal);

        // Assert
        assertEquals(mensagem, exception.getMessage());
        assertEquals(causaOriginal, exception.getCause());
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    void heranca_DeveSerRuntimeException() {
        // Arrange & Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException("Teste");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void excecao_DevePodSerLancada() {
        // Arrange
        String mensagem = "Conta 99999 não existe";

        // Act & Assert
        assertThrows(ContaNaoEncontradaException.class, () -> {
            throw new ContaNaoEncontradaException(mensagem);
        });
    }

    @Test
    void excecao_DevePodSerCapturada() {
        // Arrange
        String mensagemEsperada = "Conta inexistente";

        // Act & Assert
        try {
            throw new ContaNaoEncontradaException(mensagemEsperada);
        } catch (ContaNaoEncontradaException e) {
            assertEquals(mensagemEsperada, e.getMessage());
        } catch (Exception e) {
            fail("Deveria capturar ContaNaoEncontradaException, mas capturou: " + e.getClass().getSimpleName());
        }
    }

    @Test
    void toString_DeveConterInformacoesRelevantes() {
        // Arrange
        String mensagem = "Conta com ID 123 não encontrada";
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagem);

        // Act
        String resultado = exception.toString();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("ContaNaoEncontradaException"));
        assertTrue(resultado.contains(mensagem));
    }

    @Test
    void equals_MesmaExcecao_DeveSerIgual() {
        // Arrange
        String mensagem = "Conta não encontrada";
        ContaNaoEncontradaException exception1 = new ContaNaoEncontradaException(mensagem);
        ContaNaoEncontradaException exception2 = exception1;

        // Act & Assert
        assertEquals(exception1, exception2);
        assertEquals(exception1.hashCode(), exception2.hashCode());
    }

    @Test
    void construtor_CenarioRealista_ContaComNumero() {
        // Arrange
        String numeroConta = "12345-6";
        String mensagem = String.format("Conta com número %s não foi encontrada no sistema", numeroConta);

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagem);

        // Assert
        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception.getMessage().contains(numeroConta));
    }

    @Test
    void construtor_CenarioRealista_ComCausaBancoDados() {
        // Arrange
        String mensagem = "Falha ao consultar conta no banco de dados";
        RuntimeException causaBD = new RuntimeException("Connection timeout");

        // Act
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagem, causaBD);

        // Assert
        assertEquals(mensagem, exception.getMessage());
        assertEquals(causaBD, exception.getCause());
        assertEquals("Connection timeout", exception.getCause().getMessage());
    }
}