package br.com.banksystem.fraudes.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do VerificarRiscoDelegate")
class VerificarRiscoDelegateTest {

    @Mock
    private DelegateExecution execution;

    private VerificarRiscoDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate = new VerificarRiscoDelegate();
    }

    @Test
    @DisplayName("Deve marcar alto risco quando valor for maior que dez mil")
    void deveMarcarAltoRiscoQuandoValorForMaiorQueDezMil() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-001");
        when(execution.getVariable("valor")).thenReturn(15000.0);
        when(execution.getVariable("numeroConta")).thenReturn("12345-6");

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("altoRisco", true);
        verify(execution).setVariable("scoreRisco", 85);
    }

    @Test
    @DisplayName("Deve marcar baixo risco quando valor for menor que dez mil")
    void deveMarcarBaixoRiscoQuandoValorForMenorQueDezMil() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-002");
        when(execution.getVariable("valor")).thenReturn(5000.0);
        when(execution.getVariable("numeroConta")).thenReturn("12345-6");

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("altoRisco", false);
        verify(execution).setVariable("scoreRisco", 20);
    }

    @Test
    @DisplayName("Deve marcar baixo risco quando valor for exatamente dez mil")
    void deveMarcarBaixoRiscoQuandoValorForExatamenteDezMil() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-003");
        when(execution.getVariable("valor")).thenReturn(10000.0);
        when(execution.getVariable("numeroConta")).thenReturn("99999-9");

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("altoRisco", false);
        verify(execution).setVariable("scoreRisco", 20);
    }

    @Test
    @DisplayName("Deve marcar baixo risco quando valor for nulo")
    void deveMarcarBaixoRiscoQuandoValorForNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-004");
        when(execution.getVariable("valor")).thenReturn(null);
        when(execution.getVariable("numeroConta")).thenReturn("11111-1");

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("altoRisco", false);
        verify(execution).setVariable("scoreRisco", 20);
    }

    @Test
    @DisplayName("Deve funcionar mesmo com idTransacao e numeroConta nulos")
    void deveFuncionarMesmoComIdTransacaoENumeroContaNulos() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn(null);
        when(execution.getVariable("valor")).thenReturn(20000.0);
        when(execution.getVariable("numeroConta")).thenReturn(null);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("altoRisco", true);
        verify(execution).setVariable("scoreRisco", 85);
    }
}