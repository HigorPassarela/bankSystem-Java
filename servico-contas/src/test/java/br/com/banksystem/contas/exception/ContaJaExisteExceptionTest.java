package br.com.banksystem.contas.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContaJaExisteExceptionTest {

    @Test
    void construtor_ComMensagem_DeveDefinirMensagemCorretamente() {
        // Arrange
        String mensagemEsperada = "Conta com número 12345 já existe no sistema";

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagemEsperada);

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
        ContaJaExisteException exception = new ContaJaExisteException(mensagemNula);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void construtor_ComMensagemVazia_DeveDefinirMensagemVazia() {
        // Arrange
        String mensagemVazia = "";

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagemVazia);

        // Assert
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void construtor_ComMensagemComEspacos_DevePreservarEspacos() {
        // Arrange
        String mensagemComEspacos = "  Conta já existe  ";

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagemComEspacos);

        // Assert
        assertEquals(mensagemComEspacos, exception.getMessage());
    }

    @Test
    void heranca_DeveSerRuntimeException() {
        // Arrange & Act
        ContaJaExisteException exception = new ContaJaExisteException("Teste");

        // Assert
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void excecao_DevePodSerLancada() {
        // Arrange
        String mensagem = "Conta com CPF 123.456.789-00 já existe";

        // Act & Assert
        assertThrows(ContaJaExisteException.class, () -> {
            throw new ContaJaExisteException(mensagem);
        });
    }

    @Test
    void excecao_DevePodSerCapturada() {
        // Arrange
        String mensagemEsperada = "Conta duplicada";

        // Act & Assert
        try {
            throw new ContaJaExisteException(mensagemEsperada);
        } catch (ContaJaExisteException e) {
            assertEquals(mensagemEsperada, e.getMessage());
        } catch (Exception e) {
            fail("Deveria capturar ContaJaExisteException, mas capturou: " + e.getClass().getSimpleName());
        }
    }

    @Test
    void toString_DeveConterInformacoesRelevantes() {
        // Arrange
        String mensagem = "Conta com número 98765 já existe";
        ContaJaExisteException exception = new ContaJaExisteException(mensagem);

        // Act
        String resultado = exception.toString();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("ContaJaExisteException"));
        assertTrue(resultado.contains(mensagem));
    }

    @Test
    void getMessage_DeveRetornarMensagemOriginal() {
        // Arrange
        String mensagemOriginal = "Conflito: conta já cadastrada";
        ContaJaExisteException exception = new ContaJaExisteException(mensagemOriginal);

        // Act
        String mensagemRetornada = exception.getMessage();

        // Assert
        assertEquals(mensagemOriginal, mensagemRetornada);
    }

    @Test
    void getCause_SemCausaDefinida_DeveRetornarNull() {
        // Arrange
        ContaJaExisteException exception = new ContaJaExisteException("Mensagem teste");

        // Act
        Throwable causa = exception.getCause();

        // Assert
        assertNull(causa);
    }

    @Test
    void getStackTrace_DeveConterInformacoes() {
        // Arrange & Act
        ContaJaExisteException exception = new ContaJaExisteException("Teste stack trace");

        // Assert
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    void equals_MesmaExcecao_DeveSerIgual() {
        // Arrange
        String mensagem = "Conta já existe";
        ContaJaExisteException exception1 = new ContaJaExisteException(mensagem);
        ContaJaExisteException exception2 = exception1;

        // Act & Assert
        assertEquals(exception1, exception2);
        assertEquals(exception1.hashCode(), exception2.hashCode());
    }

    @Test
    void construtor_CenarioRealista_ContaComCPF() {
        // Arrange
        String cpf = "123.456.789-00";
        String mensagem = String.format("Já existe uma conta cadastrada para o CPF %s", cpf);

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagem);

        // Assert
        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception.getMessage().contains(cpf));
    }

    @Test
    void construtor_CenarioRealista_ContaComNumero() {
        // Arrange
        String numeroConta = "12345-6";
        String mensagem = String.format("Conta com número %s já existe no sistema", numeroConta);

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagem);

        // Assert
        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception.getMessage().contains(numeroConta));
    }

    @Test
    void construtor_CenarioRealista_ContaComEmail() {
        // Arrange
        String email = "usuario@exemplo.com";
        String mensagem = String.format("Já existe uma conta associada ao email %s", email);

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagem);

        // Assert
        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void construtor_MensagemLonga_DevePreservarConteudoCompleto() {
        // Arrange
        String mensagemLonga = "Esta é uma mensagem muito longa para testar se a exceção consegue " +
                "armazenar e retornar mensagens extensas sem perda de informação ou truncamento " +
                "de dados importantes para o debugging e logs do sistema bancário";

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagemLonga);

        // Assert
        assertEquals(mensagemLonga, exception.getMessage());
        assertEquals(mensagemLonga.length(), exception.getMessage().length());
    }

    @Test
    void construtor_MensagemComCaracteresEspeciais_DevePreservarCaracteres() {
        // Arrange
        String mensagemEspecial = "Conta já existe: áéíóú çñü @#$%&*()[]{}";

        // Act
        ContaJaExisteException exception = new ContaJaExisteException(mensagemEspecial);

        // Assert
        assertEquals(mensagemEspecial, exception.getMessage());
    }

    @Test
    void excecao_EmContextoTryCatch_DeveManterComportamentoEsperado() {
        // Arrange
        String mensagem = "Tentativa de criar conta duplicada";
        boolean excecaoCapturada = false;

        // Act
        try {
            throw new ContaJaExisteException(mensagem);
        } catch (ContaJaExisteException e) {
            excecaoCapturada = true;
            assertEquals(mensagem, e.getMessage());
        }

        // Assert
        assertTrue(excecaoCapturada, "A exceção deveria ter sido capturada");
    }

    @Test
    void excecao_ComoRuntimeException_DevePodSerCapturadaComoRuntimeException() {
        // Arrange
        String mensagem = "Conta já cadastrada";
        boolean capturadaComoRuntimeException = false;

        // Act
        try {
            throw new ContaJaExisteException(mensagem);
        } catch (RuntimeException e) {
            capturadaComoRuntimeException = true;
            assertTrue(e instanceof ContaJaExisteException);
            assertEquals(mensagem, e.getMessage());
        }

        // Assert
        assertTrue(capturadaComoRuntimeException);
    }
}