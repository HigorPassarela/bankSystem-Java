package br.com.banksystem.fraudes.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AprovarTransacaoDelegate")
class AprovarTransacaoDelegateTest {

    @Mock
    private DelegateExecution execution;

    private AprovarTransacaoDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate = new AprovarTransacaoDelegate();
    }

    @Test
    @DisplayName("Deve aprovar transação e definir resultado antifraude")
    void deveAprovarTransacaoEDefinirResultadoAntifraude() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-001");
        when(execution.getVariable("numeroConta")).thenReturn("12345-6");
        when(execution.getVariable("scoreRisco")).thenReturn(15);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).getVariable("idTransacao");
        verify(execution).getVariable("numeroConta");
        verify(execution).getVariable("scoreRisco");
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }

    @Test
    @DisplayName("Deve aprovar transação mesmo com score de risco nulo")
    void deveAprovarTransacaoMesmoComScoreRiscoNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-002");
        when(execution.getVariable("numeroConta")).thenReturn("99999-9");
        when(execution.getVariable("scoreRisco")).thenReturn(null);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }

    @Test
    @DisplayName("Deve aprovar transação mesmo com idTransacao nulo")
    void deveAprovarTransacaoMesmoComIdTransacaoNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn(null);
        when(execution.getVariable("numeroConta")).thenReturn("12345-6");
        when(execution.getVariable("scoreRisco")).thenReturn(10);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }

    @Test
    @DisplayName("Deve aprovar transação mesmo com numeroConta nulo")
    void deveAprovarTransacaoMesmoComNumeroContaNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-003");
        when(execution.getVariable("numeroConta")).thenReturn(null);
        when(execution.getVariable("scoreRisco")).thenReturn(5);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }
}