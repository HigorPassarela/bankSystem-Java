package br.com.banksystem.notificacoes.kafka;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.dto.TransacaoEventoDTO;
import br.com.banksystem.notificacoes.service.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka que encaminha eventos de transação como notificações SSE.
 */
@Component
public class NotificacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoConsumer.class);
    private final SseEmitterService sseEmitterService;

    public NotificacaoConsumer(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @KafkaListener(topics = "transacoes-aprovadas", groupId = "servico-notificacoes")
    public void consumirAprovada(TransacaoEventoDTO evento) {
        log.info("Notificando transação aprovada: {} - conta: {}", evento.idTransacao(), evento.numeroConta());
        String msg = String.format("Transação de %s no valor de R$ %.2f aprovada com sucesso",
                evento.tipo(), evento.valor());
        sseEmitterService.enviarNotificacao(evento.numeroConta(),
                NotificacaoDTO.criar("TRANSACAO_APROVADA", msg, evento));
    }

    @KafkaListener(topics = "transacoes-reprovadas", groupId = "servico-notificacoes")
    public void consumirReprovada(TransacaoEventoDTO evento) {
        log.info("Notificando transação reprovada: {} - conta: {}", evento.idTransacao(), evento.numeroConta());
        String msg = String.format("Transação de %s no valor de R$ %.2f reprovada por saldo insuficiente",
                evento.tipo(), evento.valor());
        sseEmitterService.enviarNotificacao(evento.numeroConta(),
                NotificacaoDTO.criar("TRANSACAO_REPROVADA", msg, evento));
    }
}
