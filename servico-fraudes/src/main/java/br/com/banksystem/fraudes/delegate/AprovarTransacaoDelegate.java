package br.com.banksystem.fraudes.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Delegate Camunda para aprovar transações de baixo risco.
 */
@Component("aprovarTransacaoDelegate")
public class AprovarTransacaoDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(AprovarTransacaoDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String idTransacao = (String) execution.getVariable("idTransacao");
        String numeroConta = (String) execution.getVariable("numeroConta");
        Integer scoreRisco = (Integer) execution.getVariable("scoreRisco");

        log.info("Aprovando transação {} da conta {} após análise antifraude. Score={}",
                idTransacao, numeroConta, scoreRisco);

        execution.setVariable("resultadoAntifraude", "APROVADA_ANTIFRAUDE");
    }
}