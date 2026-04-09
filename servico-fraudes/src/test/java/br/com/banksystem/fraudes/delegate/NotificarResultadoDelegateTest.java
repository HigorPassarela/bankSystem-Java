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
@DisplayName("Testes do NotificarResultadoDelegate")
class NotificarResultadoDelegateTest {

    @Mock
    private DelegateExecution execution;

    private NotificarResultadoDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate = new NotificarResultadoDelegate();
    }

    @Test
    @DisplayName("Deve definir resultado como suspeita fraude quando alto risco for true")
    void deveDefinirResultadoComoSuspeitaFraudeQuandoAltoRiscoForTrue() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-001");
        when(execution.getVariable("altoRisco")).thenReturn(true);
        when(execution.getVariable("scoreRisco")).thenReturn(95);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).getVariable("idTransacao");
        verify(execution).getVariable("altoRisco");
        verify(execution).getVariable("scoreRisco");
        verify(execution).setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");
    }

    @Test
    @DisplayName("Deve definir resultado como aprovada antifraude quando alto risco for false")
    void deveDefinirResultadoComoAprovadaAntifraudeQuandoAltoRiscoForFalse() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-002");
        when(execution.getVariable("altoRisco")).thenReturn(false);
        when(execution.getVariable("scoreRisco")).thenReturn(15);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }

    @Test
    @DisplayName("Deve definir resultado como aprovada antifraude quando alto risco for nulo")
    void deveDefinirResultadoComoAprovadaAntifraudeQuandoAltoRiscoForNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-003");
        when(execution.getVariable("altoRisco")).thenReturn(null);
        when(execution.getVariable("scoreRisco")).thenReturn(30);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }

    @Test
    @DisplayName("Deve funcionar mesmo com idTransacao nulo")
    void deveFuncionarMesmoComIdTransacaoNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn(null);
        when(execution.getVariable("altoRisco")).thenReturn(true);
        when(execution.getVariable("scoreRisco")).thenReturn(99);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");
    }

    @Test
    @DisplayName("Deve funcionar mesmo com scoreRisco nulo")
    void deveFuncionarMesmoComScoreRiscoNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-004");
        when(execution.getVariable("altoRisco")).thenReturn(false);
        when(execution.getVariable("scoreRisco")).thenReturn(null);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }
}