package br.com.banksystem.fraudes.service;

import br.com.banksystem.fraudes.dto.FraudeDetectadaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class CamundaExternalTaskWorker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CamundaExternalTaskWorker.class);

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, FraudeDetectadaDTO> fraudeKafkaTemplate;

    @Value("${servico.camunda.url}")
    private String camundaUrl;

    public CamundaExternalTaskWorker(
            RestTemplate restTemplate,
            @Qualifier("fraudeKafkaTemplate") KafkaTemplate<String, FraudeDetectadaDTO> fraudeKafkaTemplate
    ) {
        this.restTemplate = restTemplate;
        this.fraudeKafkaTemplate = fraudeKafkaTemplate;
    }

    @Override
    public void run(String... args) {
        startWorker("verificar-risco");
        startWorker("aprovar-transacao");
        startWorker("bloquear-transacao");
    }

    private void startWorker(String topic) {
        final String workerId = "fraudes-worker-" + topic;

        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    Map<String, Object> body = Map.of(
                            "workerId", workerId,
                            "maxTasks", 1,
                            "topics", List.of(Map.of(
                                    "topicName", topic,
                                    "lockDuration", 10000
                            ))
                    );

                    ResponseEntity<JsonNode[]> response = restTemplate.postForEntity(
                            camundaUrl + "/external-task/fetchAndLock",
                            new HttpEntity<>(body, headers),
                            JsonNode[].class
                    );

                    JsonNode[] tasks = response.getBody();
                    if (tasks != null && tasks.length > 0) {
                        for (JsonNode task : tasks) {
                            switch (topic) {
                                case "verificar-risco" -> handleVerificarRisco(task, workerId);
                                case "aprovar-transacao" -> handleAprovar(task, workerId);
                                case "bloquear-transacao" -> handleBloquear(task, workerId);
                            }
                        }
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Erro no worker do tópico {}: {}", topic, e.getMessage(), e);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    private void handleVerificarRisco(JsonNode task, String workerId) {
        String taskId = task.get("id").asText();
        JsonNode vars = task.get("variables");

        double valor = vars.get("valor").get("value").asDouble();
        boolean altoRisco = valor > 10000.0;
        int scoreRisco = altoRisco ? 85 : 20;

        try {
            Thread.sleep(3000); // só para visualização no cockpit
        } catch (InterruptedException ignored) {}

        completeTask(taskId, workerId, Map.of(
                "altoRisco", Map.of("value", altoRisco, "type", "Boolean"),
                "scoreRisco", Map.of("value", scoreRisco, "type", "Integer")
        ));

        log.info("ExternalTask verificar-risco concluída. valor={} altoRisco={}", valor, altoRisco);
    }

    private void handleAprovar(JsonNode task, String workerId) {
        String taskId = task.get("id").asText();

        completeTask(taskId, workerId, Map.of(
                "resultadoAntifraude", Map.of("value", "APROVADA_ANTIFRAUDE", "type", "String")
        ));

        log.info("ExternalTask aprovar-transacao concluída");
    }

    private void handleBloquear(JsonNode task, String workerId) {
        String taskId = task.get("id").asText();
        JsonNode vars = task.get("variables");

        String idTransacao = vars.get("idTransacao").get("value").asText();
        String numeroConta = vars.get("numeroConta").get("value").asText();
        String tipo = vars.get("tipo").get("value").asText();
        double valor = vars.get("valor").get("value").asDouble();
        String contaOrigem = vars.has("contaOrigem") ? vars.get("contaOrigem").get("value").asText() : null;
        String contaDestino = vars.has("contaDestino") ? vars.get("contaDestino").get("value").asText() : null;
        int scoreRisco = vars.get("scoreRisco").get("value").asInt();

        fraudeKafkaTemplate.send(
                "transacoes-suspeitas",
                numeroConta,
                new FraudeDetectadaDTO(
                        idTransacao,
                        numeroConta,
                        BigDecimal.valueOf(valor),
                        tipo,
                        scoreRisco,
                        "SUSPEITA_FRAUDE",
                        LocalDateTime.now(),
                        contaOrigem,
                        contaDestino
                )
        );

        completeTask(taskId, workerId, Map.of(
                "resultadoAntifraude", Map.of("value", "SUSPEITA_FRAUDE", "type", "String")
        ));

        log.warn("ExternalTask bloquear-transacao concluída e fraude publicada para {}", idTransacao);
    }

    private void completeTask(String taskId, String workerId, Map<String, Object> variables) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "workerId", workerId,
                "variables", variables
        );

        restTemplate.postForEntity(
                camundaUrl + "/external-task/" + taskId + "/complete",
                new HttpEntity<>(body, headers),
                String.class
        );
    }
}