package br.com.banksystem.fraudes.delegate;

import br.com.banksystem.fraudes.dto.FraudeDetectadaDTO;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Delegate Camunda para marcar transações de alto risco e publicar evento de compensação.
 */
@Component("bloquearTransacaoDelegate")
public class BloquearTransacaoDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(BloquearTransacaoDelegate.class);

    private final KafkaTemplate<String, FraudeDetectadaDTO> fraudeKafkaTemplate;

    public BloquearTransacaoDelegate(
            @Qualifier("fraudeKafkaTemplate")
            KafkaTemplate<String, FraudeDetectadaDTO> fraudeKafkaTemplate
    ) {
        this.fraudeKafkaTemplate = fraudeKafkaTemplate;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String idTransacao = (String) execution.getVariable("idTransacao");
        String numeroConta = (String) execution.getVariable("numeroConta");
        Integer scoreRisco = (Integer) execution.getVariable("scoreRisco");
        Double valorDouble = (Double) execution.getVariable("valor");
        String tipo = (String) execution.getVariable("tipo");
        String contaOrigem = (String) execution.getVariable("contaOrigem");
        String contaDestino = (String) execution.getVariable("contaDestino");

        log.warn("Transação {} da conta {} marcada como suspeita de fraude. Score={}",
                idTransacao, numeroConta, scoreRisco);

        execution.setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");

        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao,
                numeroConta,
                valorDouble != null ? BigDecimal.valueOf(valorDouble) : BigDecimal.ZERO,
                tipo,
                scoreRisco,
                "SUSPEITA_FRAUDE",
                LocalDateTime.now(),
                contaOrigem,
                contaDestino
        );

        fraudeKafkaTemplate.send("transacoes-suspeitas", numeroConta, evento);
        log.warn("Evento de fraude suspeita publicado para transação {}", idTransacao);
    }
}