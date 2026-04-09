package br.com.banksystem.notificacoes.kafka;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.dto.TransacaoEventoDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificacaoConsumer")
class NotificacaoConsumerTest {

    @Mock
    private SseEmitterService sseEmitterService;

    private NotificacaoConsumer notificacaoConsumer;

    @BeforeEach
    void setUp() {
        notificacaoConsumer = new NotificacaoConsumer(sseEmitterService);
    }

    @Test
    @DisplayName("Deve ignorar TRANSFERENCIA_ENTRADA em transação aprovada")
    void deveIgnorarTransferenciaEntradaEmTransacaoAprovada() {
        // Given
        TransacaoEventoDTO evento = criarEvento("TRANSFERENCIA_ENTRADA", new BigDecimal("100.00"));

        // When
        notificacaoConsumer.consumirAprovada(evento);

        // Then
        verify(sseEmitterService, never()).enviarNotificacao(anyString(), any(NotificacaoDTO.class));
    }

    @Test
    @DisplayName("Deve notificar depósito aprovado")
    void deveNotificarDepositoAprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("DEPOSITO", new BigDecimal("200.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirAprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.tipo()).isEqualTo("TRANSACAO_APROVADA");
        assertThat(notificacao.mensagem()).contains("Depósito");
        assertThat(notificacao.mensagem()).contains("realizado com sucesso");
        assertThat(notificacao.dados()).isEqualTo(evento);
        assertThat(notificacao.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve notificar débito aprovado")
    void deveNotificarDebitoAprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("DEBITO", new BigDecimal("150.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirAprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.tipo()).isEqualTo("TRANSACAO_APROVADA");
        assertThat(notificacao.mensagem()).contains("Débito");
        assertThat(notificacao.mensagem()).contains("realizado com sucesso");
    }

    @Test
    @DisplayName("Deve notificar crédito aprovado")
    void deveNotificarCreditoAprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("CREDITO", new BigDecimal("300.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirAprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.mensagem()).contains("Crédito");
        assertThat(notificacao.mensagem()).contains("realizado com sucesso");
    }

    @Test
    @DisplayName("Deve notificar transferência de saída aprovada")
    void deveNotificarTransferenciaSaidaAprovada() {
        // Given
        TransacaoEventoDTO evento = criarEvento("TRANSFERENCIA_SAIDA", new BigDecimal("500.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirAprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.mensagem()).contains("Transferência");
        assertThat(notificacao.mensagem()).contains("realizada com sucesso");
    }

    @Test
    @DisplayName("Deve notificar tipo genérico aprovado")
    void deveNotificarTipoGenericoAprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("PIX", new BigDecimal("50.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirAprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.mensagem()).contains("Transação de PIX");
        assertThat(notificacao.mensagem()).contains("aprovada com sucesso");
    }

    @Test
    @DisplayName("Deve notificar débito reprovado")
    void deveNotificarDebitoReprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("DEBITO", new BigDecimal("120.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirReprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.tipo()).isEqualTo("TRANSACAO_REPROVADA");
        assertThat(notificacao.mensagem()).contains("Débito");
        assertThat(notificacao.mensagem()).contains("saldo insuficiente");
        assertThat(notificacao.dados()).isEqualTo(evento);
    }

    @Test
    @DisplayName("Deve notificar crédito reprovado")
    void deveNotificarCreditoReprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("CREDITO", new BigDecimal("220.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirReprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.mensagem()).contains("Crédito");
        assertThat(notificacao.mensagem()).contains("limite insuficiente");
    }

    @Test
    @DisplayName("Deve notificar transferência de saída reprovada")
    void deveNotificarTransferenciaSaidaReprovada() {
        // Given
        TransacaoEventoDTO evento = criarEvento("TRANSFERENCIA_SAIDA", new BigDecimal("400.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirReprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.mensagem()).contains("Transferência");
        assertThat(notificacao.mensagem()).contains("saldo insuficiente");
    }

    @Test
    @DisplayName("Deve notificar tipo genérico reprovado")
    void deveNotificarTipoGenericoReprovado() {
        // Given
        TransacaoEventoDTO evento = criarEvento("PIX", new BigDecimal("75.00"));
        ArgumentCaptor<NotificacaoDTO> captor = ArgumentCaptor.forClass(NotificacaoDTO.class);

        // When
        notificacaoConsumer.consumirReprovada(evento);

        // Then
        verify(sseEmitterService).enviarNotificacao(eq("12345-6"), captor.capture());

        NotificacaoDTO notificacao = captor.getValue();
        assertThat(notificacao.tipo()).isEqualTo("TRANSACAO_REPROVADA");
        assertThat(notificacao.mensagem()).contains("Transação de PIX");
        assertThat(notificacao.mensagem()).contains("foi reprovada");
    }

    private TransacaoEventoDTO criarEvento(String tipo, BigDecimal valor) {
        return new TransacaoEventoDTO(
                "tx-001",
                "12345-6",
                valor,
                tipo,
                "APROVADA",
                "Descrição teste",
                LocalDateTime.now(),
                new BigDecimal("1000.00"),
                "12345-6",
                "99999-9"
        );
    }
}