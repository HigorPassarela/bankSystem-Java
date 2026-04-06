package br.com.banksystem.fraudes.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Delegate Camunda para marcar transações de alto risco.
 */
@Component("bloquearTransacaoDelegate")
public class BloquearTransacaoDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(BloquearTransacaoDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String idTransacao = (String) execution.getVariable("idTransacao");
        String numeroConta = (String) execution.getVariable("numeroConta");
        Integer scoreRisco = (Integer) execution.getVariable("scoreRisco");

        log.warn("Transação {} da conta {} marcada como suspeita de fraude. Score={}",
                idTransacao, numeroConta, scoreRisco);

        execution.setVariable("resultadoAntifraude", "SUSPEITA_FRAUDE");
    }
}