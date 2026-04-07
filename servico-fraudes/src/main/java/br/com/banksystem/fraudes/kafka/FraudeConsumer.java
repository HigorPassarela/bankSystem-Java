package br.com.banksystem.fraudes.kafka;

import br.com.banksystem.fraudes.dto.TransacaoEventoDTO;
import br.com.banksystem.fraudes.service.AnaliseFraudeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka que inicia análise de fraude para cada transação aprovada.
 */
@Component
public class FraudeConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudeConsumer.class);
    private final AnaliseFraudeService analiseFraudeService;

    public FraudeConsumer(AnaliseFraudeService analiseFraudeService) {
        this.analiseFraudeService = analiseFraudeService;
    }

    @KafkaListener(
            topics = "transacoes-aprovadas",
            groupId = "servico-fraudes",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirTransacaoAprovada(TransacaoEventoDTO evento) {
        log.info("Iniciando análise de fraude para transação: {}", evento.idTransacao());
        analiseFraudeService.iniciarAnalise(evento);
    }
}