package br.com.banksystem.fraudes.service;

import br.com.banksystem.fraudes.dto.TransacaoEventoDTO;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço que inicia processos de análise de fraude via Camunda 7.
 */
@Service
public class AnaliseFraudeService {

    private static final Logger log = LoggerFactory.getLogger(AnaliseFraudeService.class);
    private static final String PROCESSO_FRAUDE = "processoAnaliseFraude";

    private final RuntimeService runtimeService;

    public AnaliseFraudeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Inicia um processo Camunda de análise de fraude para a transação recebida.
     */
    public void iniciarAnalise(TransacaoEventoDTO evento) {
        try {
            Map<String, Object> variaveis = new HashMap<>();
            variaveis.put("idTransacao", evento.idTransacao());
            variaveis.put("numeroConta", evento.numeroConta());
            variaveis.put("valor", evento.valor().doubleValue());
            variaveis.put("tipo", evento.tipo());
            variaveis.put("dataHora", evento.dataHora() != null ? evento.dataHora().toString() : "");

            runtimeService.startProcessInstanceByKey(PROCESSO_FRAUDE, evento.idTransacao(), variaveis);
            log.info("Processo de análise de fraude iniciado para transação: {}", evento.idTransacao());
        } catch (Exception ex) {
            log.error("Erro ao iniciar processo de fraude para transação {}: {}", evento.idTransacao(), ex.getMessage());
        }
    }
}
