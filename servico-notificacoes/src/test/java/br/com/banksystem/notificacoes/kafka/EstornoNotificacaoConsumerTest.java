package br.com.banksystem.notificacoes.kafka;

import br.com.banksystem.notificacoes.dto.EstornoFraudeDTO;
import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.service.SseEmitterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EstornoNotificacaoConsumer")
class EstornoNotificacaoConsumerTest {

    @Mock
    private SseEmitterService sseEmitterService;

    private EstornoNotificacaoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EstornoNotificacaoConsumer(sseEmitterService);
    }

    @Test
    @DisplayName("Deve consumir estorno e enviar notificação SSE")
    void deveConsumirEstornoEEnviarNotificacaoSse() {
        // Given
        EstornoFraudeDTO evento = new EstornoFraudeDTO(
                "tx-001",
                "12345-6",
                new BigDecimal("250.75"),
                "TRANSFERENCIA_SAIDA",
                "Suspeita de fraude",
                LocalDateTime.now(),
                "12345-6",
                "99999-9"
        );

        ArgumentCaptor<NotificacaoDTO> notificacaoCaptor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        consumer.consumirEstorno(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), notificacaoCaptor.capture());

        NotificacaoDTO notificacao = notificacaoCaptor.getValue();
        assertThat(notificacao).isNotNull();
        assertThat(notificacao.tipo()).isEqualTo("TRANSACAO_ESTORNADA");
        assertThat(notificacao.mensagem()).contains("Transação estornada por suspeita de fraude");
        assertThat(notificacao.mensagem()).contains("Tipo: TRANSFERENCIA_SAIDA");
        assertThat(notificacao.mensagem()).contains("Valor: R$");
        assertThat(notificacao.dados()).isEqualTo(evento);
        assertThat(notificacao.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve enviar notificação com evento no campo dados")
    void deveEnviarNotificacaoComEventoNoCampoDados() {
        // Given
        EstornoFraudeDTO evento = new EstornoFraudeDTO(
                "tx-002",
                "54321-0",
                new BigDecimal("100.00"),
                "DEBITO",
                "Fraude detectada",
                LocalDateTime.now(),
                "54321-0",
                null
        );

        ArgumentCaptor<NotificacaoDTO> notificacaoCaptor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        consumer.consumirEstorno(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("54321-0"), notificacaoCaptor.capture());

        NotificacaoDTO notificacao = notificacaoCaptor.getValue();
        assertThat(notificacao.tipo()).isEqualTo("TRANSACAO_ESTORNADA");
        assertThat(notificacao.dados()).isEqualTo(evento);
        assertThat(notificacao.timestamp()).isNotNull();
    }
}