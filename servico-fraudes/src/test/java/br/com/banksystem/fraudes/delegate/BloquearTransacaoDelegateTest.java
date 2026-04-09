package br.com.banksystem.fraudes.delegate;

import br.com.banksystem.fraudes.dto.FraudeDetectadaDTO;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do BloquearTransacaoDelegate")
class BloquearTransacaoDelegateTest {

    @Mock
    private DelegateExecution execution;

    @Mock
    private KafkaTemplate<String, FraudeDetectadaDTO> fraudeKafkaTemplate;

    private BloquearTransacaoDelegate delegate;

    @BeforeEach
    void setUp() {
        delegate = new BloquearTransacaoDelegate(fraudeKafkaTemplate);
    }

    @Test
    @DisplayName("Deve bloquear transação e publicar evento de fraude")
    void deveBloquearTransacaoEPublicarEventoDeFraude() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-001");
        when(execution.getVariable("numeroConta")).thenReturn("12345-6");
        when(execution.getVariable("scoreRisco")).thenReturn(95);
        when(execution.getVariable("valor")).thenReturn(1500.75d);
        when(execution.getVariable("tipo")).thenReturn("TRANSFERENCIA");
        when(execution.getVariable("contaOrigem")).thenReturn("12345-6");
        when(execution.getVariable("contaDestino")).thenReturn("99999-9");

        ArgumentCaptor<FraudeDetectadaDTO> eventoCaptor = ArgumentCaptor.forClass(FraudeDetectadaDTO.class);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");
        verify(fraudeKafkaTemplate).send(eq("transacoes-suspeitas"), eq("12345-6"), eventoCaptor.capture());

        FraudeDetectadaDTO evento = eventoCaptor.getValue();
        assertThat(evento).isNotNull();
        assertThat(evento.idTransacao()).isEqualTo("tx-001");
        assertThat(evento.numeroConta()).isEqualTo("12345-6");
        assertThat(evento.valor()).isEqualByComparingTo(BigDecimal.valueOf(1500.75d));
        assertThat(evento.tipo()).isEqualTo("TRANSFERENCIA");
        assertThat(evento.scoreRisco()).isEqualTo(95);
        assertThat(evento.resultadoAntifraude()).isEqualTo("SUSPEITA_FRAUDE");
        assertThat(evento.dataHora()).isNotNull();
        assertThat(evento.contaOrigem()).isEqualTo("12345-6");
        assertThat(evento.contaDestino()).isEqualTo("99999-9");
    }

    @Test
    @DisplayName("Deve usar BigDecimal zero quando valor for nulo")
    void deveUsarBigDecimalZeroQuandoValorForNulo() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn("tx-002");
        when(execution.getVariable("numeroConta")).thenReturn("54321-0");
        when(execution.getVariable("scoreRisco")).thenReturn(99);
        when(execution.getVariable("valor")).thenReturn(null);
        when(execution.getVariable("tipo")).thenReturn("DEBITO");
        when(execution.getVariable("contaOrigem")).thenReturn("54321-0");
        when(execution.getVariable("contaDestino")).thenReturn(null);

        ArgumentCaptor<FraudeDetectadaDTO> eventoCaptor = ArgumentCaptor.forClass(FraudeDetectadaDTO.class);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");
        verify(fraudeKafkaTemplate).send(eq("transacoes-suspeitas"), eq("54321-0"), eventoCaptor.capture());

        FraudeDetectadaDTO evento = eventoCaptor.getValue();
        assertThat(evento.valor()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(evento.contaDestino()).isNull();
    }

    @Test
    @DisplayName("Deve publicar evento mesmo com campos opcionais nulos")
    void devePublicarEventoMesmoComCamposOpcionaisNulos() {
        // Given
        when(execution.getVariable("idTransacao")).thenReturn(null);
        when(execution.getVariable("numeroConta")).thenReturn("11111-1");
        when(execution.getVariable("scoreRisco")).thenReturn(null);
        when(execution.getVariable("valor")).thenReturn(100.0d);
        when(execution.getVariable("tipo")).thenReturn(null);
        when(execution.getVariable("contaOrigem")).thenReturn(null);
        when(execution.getVariable("contaDestino")).thenReturn(null);

        ArgumentCaptor<FraudeDetectadaDTO> eventoCaptor = ArgumentCaptor.forClass(FraudeDetectadaDTO.class);

        // When
        delegate.execute(execution);

        // Then
        verify(execution).setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");
        verify(fraudeKafkaTemplate).send(eq("transacoes-suspeitas"), eq("11111-1"), eventoCaptor.capture());

        FraudeDetectadaDTO evento = eventoCaptor.getValue();
        assertThat(evento.idTransacao()).isNull();
        assertThat(evento.numeroConta()).isEqualTo("11111-1");
        assertThat(evento.scoreRisco()).isNull();
        assertThat(evento.tipo()).isNull();
        assertThat(evento.contaOrigem()).isNull();
        assertThat(evento.contaDestino()).isNull();
    }
}