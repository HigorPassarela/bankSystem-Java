package br.com.banksystem.fraudes.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Delegate Camunda para notificar o resultado da análise de fraude.
 */
@Component("notificarResultadoDelegate")
public class NotificarResultadoDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(NotificarResultadoDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String idTransacao = (String) execution.getVariable("idTransacao");
        Boolean altoRisco = (Boolean) execution.getVariable("altoRisco");
        Integer scoreRisco = (Integer) execution.getVariable("scoreRisco");

        log.info("Notificando resultado da análise de fraude - transação: {}, altoRisco: {}, score: {}",
                idTransacao, altoRisco, scoreRisco);

        String resultado = Boolean.TRUE.equals(altoRisco) ? "SUSPEITA_FRAUDE" : "APROVADA_ANTIFRAUDE";
        execution.setVariable("resultadoAntifraude", resultado);
        log.info("Resultado antifraude registrado: {} para transação {}", resultado, idTransacao);
    }
}
