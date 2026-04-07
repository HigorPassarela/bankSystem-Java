package br.com.banksystem.notificacoes.kafka;

import br.com.banksystem.notificacoes.dto.EstornoFraudeDTO;
import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.service.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka que envia notificação SSE quando uma transação é estornada por fraude.
 */
@Component
public class EstornoNotificacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstornoNotificacaoConsumer.class);

    private final SseEmitterService sseEmitterService;

    public EstornoNotificacaoConsumer(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @KafkaListener(
            topics = "transacoes-estornadas",
            groupId = "servico-notificacoes-estorno",
            containerFactory = "estornoKafkaListenerContainerFactory"
    )
    public void consumirEstorno(EstornoFraudeDTO evento) {
        log.warn("Enviando notificação de estorno para conta {} referente à transação {}",
                evento.numeroConta(), evento.idTransacaoOriginal());

        String mensagem = String.format(
                "Transação estornada por suspeita de fraude. Tipo: %s | Valor: R$ %.2f",
                evento.tipoOriginal(),
                evento.valor()
        );

        sseEmitterService.enviarNotificacao(
                evento.numeroConta(),
                NotificacaoDTO.criar("TRANSACAO_ESTORNADA", mensagem, evento)
        );
    }
}