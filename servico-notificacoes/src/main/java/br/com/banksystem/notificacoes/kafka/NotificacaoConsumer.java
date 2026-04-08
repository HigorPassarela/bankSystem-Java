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

    @KafkaListener(
            topics = "transacoes-aprovadas",
            groupId = "servico-notificacoes",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirAprovada(TransacaoEventoDTO evento) {
        // Não notificar TRANSFERENCIA_ENTRADA para evitar confusão
        if ("TRANSFERENCIA_ENTRADA".equals(evento.tipo())) {
            log.info("Notificação ignorada para TRANSFERENCIA_ENTRADA da conta {}", evento.numeroConta());
            return;
        }

        log.info("Notificando transação aprovada: {} - conta: {}", evento.idTransacao(), evento.numeroConta());

        String msg = switch (evento.tipo()) {
            case "DEPOSITO" -> String.format(
                    "Depósito no valor de R$ %.2f realizado com sucesso",
                    evento.valor()
            );
            case "DEBITO" -> String.format(
                    "Débito no valor de R$ %.2f realizado com sucesso",
                    evento.valor()
            );
            case "CREDITO" -> String.format(
                    "Crédito no valor de R$ %.2f realizado com sucesso",
                    evento.valor()
            );
            case "TRANSFERENCIA_SAIDA" -> String.format(
                    "Transferência no valor de R$ %.2f realizada com sucesso",
                    evento.valor()
            );
            default -> String.format(
                    "Transação de %s no valor de R$ %.2f aprovada com sucesso",
                    evento.tipo(),
                    evento.valor()
            );
        };

        sseEmitterService.enviarNotificacao(
                evento.numeroConta(),
                NotificacaoDTO.criar("TRANSACAO_APROVADA", msg, evento)
        );
    }

    @KafkaListener(
            topics = "transacoes-reprovadas",
            groupId = "servico-notificacoes",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirReprovada(TransacaoEventoDTO evento) {
        log.info("Notificando transação reprovada: {} - conta: {}", evento.idTransacao(), evento.numeroConta());

        String msg = switch (evento.tipo()) {
            case "DEBITO" -> String.format(
                    "Débito no valor de R$ %.2f não foi realizado por saldo insuficiente",
                    evento.valor()
            );
            case "CREDITO" -> String.format(
                    "Crédito no valor de R$ %.2f não foi realizado por limite insuficiente",
                    evento.valor()
            );
            case "TRANSFERENCIA_SAIDA" -> String.format(
                    "Transferência no valor de R$ %.2f não foi realizada por saldo insuficiente",
                    evento.valor()
            );
            default -> String.format(
                    "Transação de %s no valor de R$ %.2f foi reprovada",
                    evento.tipo(),
                    evento.valor()
            );
        };

        sseEmitterService.enviarNotificacao(
                evento.numeroConta(),
                NotificacaoDTO.criar("TRANSACAO_REPROVADA", msg, evento)
        );
    }
}
