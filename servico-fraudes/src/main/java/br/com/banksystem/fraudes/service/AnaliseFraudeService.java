package br.com.banksystem.fraudes.service;

import br.com.banksystem.fraudes.dto.CamundaStartProcessRequestDTO;
import br.com.banksystem.fraudes.dto.CamundaVariableDTO;
import br.com.banksystem.fraudes.dto.TransacaoEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço que inicia processos de análise de fraude via API REST do Camunda.
 */
@Service
public class AnaliseFraudeService {

    private static final Logger log = LoggerFactory.getLogger(AnaliseFraudeService.class);
    private static final String PROCESSO_FRAUDE = "processoAnaliseFraude";

    private final RestTemplate restTemplate;

    @Value("${servico.camunda.url}")
    private String camundaUrl;

    public AnaliseFraudeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Inicia um processo Camunda via REST no container Camunda externo.
     */
    public void iniciarAnalise(TransacaoEventoDTO evento) {
        try {
            Map<String, CamundaVariableDTO> variaveis = new HashMap<>();
            variaveis.put("idTransacao", new CamundaVariableDTO(evento.idTransacao(), "String"));
            variaveis.put("numeroConta", new CamundaVariableDTO(evento.numeroConta(), "String"));
            variaveis.put("valor", new CamundaVariableDTO(evento.valor().doubleValue(), "Double"));
            variaveis.put("tipo", new CamundaVariableDTO(evento.tipo(), "String"));
            variaveis.put("dataHora", new CamundaVariableDTO(
                    evento.dataHora() != null ? evento.dataHora().toString() : "", "String"));
            variaveis.put("contaOrigem", new CamundaVariableDTO(
                    evento.contaOrigem() != null ? evento.contaOrigem() : "", "String"));
            variaveis.put("contaDestino", new CamundaVariableDTO(
                    evento.contaDestino() != null ? evento.contaDestino() : "", "String"));

            CamundaStartProcessRequestDTO body =
                    new CamundaStartProcessRequestDTO(evento.idTransacao(), variaveis);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CamundaStartProcessRequestDTO> request = new HttpEntity<>(body, headers);

            String endpoint = camundaUrl + "/process-definition/key/" + PROCESSO_FRAUDE + "/start";

            restTemplate.postForEntity(endpoint, request, String.class);

            log.info("Processo de análise de fraude iniciado via Camunda REST para transação: {}", evento.idTransacao());
        } catch (Exception ex) {
            log.error("Erro ao iniciar processo de fraude para transação {}: {}", evento.idTransacao(), ex.getMessage(), ex);
        }
    }
}