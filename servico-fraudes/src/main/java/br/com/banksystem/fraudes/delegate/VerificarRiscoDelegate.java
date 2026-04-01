package br.com.banksystem.fraudes.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Delegate Camunda para verificação de risco da transação.
 * Avalia se o valor ou padrão da transação indica possível fraude.
 */
@Component("verificarRiscoDelegate")
public class VerificarRiscoDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(VerificarRiscoDelegate.class);
    private static final double VALOR_ALTO_RISCO = 10000.0;

    @Override
    public void execute(DelegateExecution execution) {
        String idTransacao = (String) execution.getVariable("idTransacao");
        Double valor = (Double) execution.getVariable("valor");
        String numeroConta = (String) execution.getVariable("numeroConta");

        log.info("Verificando risco da transação: {} - valor: {} - conta: {}", idTransacao, valor, numeroConta);

        boolean altoRisco = valor != null && valor > VALOR_ALTO_RISCO;
        execution.setVariable("altoRisco", altoRisco);
        execution.setVariable("scoreRisco", altoRisco ? 85 : 20);

        log.info("Análise de risco concluída para transação {}: altoRisco={}", idTransacao, altoRisco);
    }
}
