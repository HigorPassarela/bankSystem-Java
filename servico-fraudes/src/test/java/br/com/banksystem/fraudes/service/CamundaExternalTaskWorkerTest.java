package br.com.banksystem.fraudes.service;

import br.com.banksystem.fraudes.dto.FraudeDetectadaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Testes frágeis do CamundaExternalTaskWorker")
class CamundaExternalTaskWorkerTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, FraudeDetectadaDTO> fraudeKafkaTemplate;

    private CamundaExternalTaskWorker worker;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        fraudeKafkaTemplate = mock(KafkaTemplate.class);

        worker = new CamundaExternalTaskWorker(restTemplate, fraudeKafkaTemplate);
        ReflectionTestUtils.setField(worker, "camundaUrl", "http://localhost:8080/engine-rest");
    }

    @Test
    @DisplayName("Deve iniciar execução do worker e tentar buscar tarefas")
    void deveIniciarExecucaoDoWorkerETentarBuscarTarefas() throws Exception {
        // Given
        when(restTemplate.postForEntity(
                contains("/external-task/fetchAndLock"),
                any(),
                eq(JsonNode[].class)
        )).thenReturn(ResponseEntity.ok(new JsonNode[0]));

        // When
        Thread thread = Thread.startVirtualThread(() -> worker.run());

        // espera um pouco para as threads internas iniciarem
        Thread.sleep(300);

        // Then
        verify(restTemplate, atLeastOnce()).postForEntity(
                contains("/external-task/fetchAndLock"),
                any(),
                eq(JsonNode[].class)
        );

        // limpeza do teste
        thread.interrupt();
    }

    @Test
    @DisplayName("Não deve quebrar quando ocorrer erro no fetch and lock")
    void naoDeveQuebrarQuandoOcorrerErroNoFetchAndLock() throws Exception {
        // Given
        when(restTemplate.postForEntity(
                contains("/external-task/fetchAndLock"),
                any(),
                eq(JsonNode[].class)
        )).thenThrow(new RuntimeException("Erro no Camunda"));

        // When
        Thread thread = Thread.startVirtualThread(() -> worker.run());

        Thread.sleep(300);

        // Then
        verify(restTemplate, atLeastOnce()).postForEntity(
                contains("/external-task/fetchAndLock"),
                any(),
                eq(JsonNode[].class)
        );

        thread.interrupt();
    }
}