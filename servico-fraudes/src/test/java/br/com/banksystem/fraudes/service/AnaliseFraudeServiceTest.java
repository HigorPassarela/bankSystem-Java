package br.com.banksystem.fraudes.service;

import br.com.banksystem.fraudes.dto.CamundaStartProcessRequestDTO;
import br.com.banksystem.fraudes.dto.CamundaVariableDTO;
import br.com.banksystem.fraudes.dto.TransacaoEventoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AnaliseFraudeService")
class AnaliseFraudeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AnaliseFraudeService analiseFraudeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(analiseFraudeService, "camundaUrl", "http://localhost:8080/engine-rest");
    }

    @Test
    @DisplayName("Deve iniciar análise de fraude com sucesso")
    void deveIniciarAnaliseFraudeComSucesso() {
        // Given
        TransacaoEventoDTO evento = new TransacaoEventoDTO(
                "tx-001",
                "12345-6",
                new BigDecimal("1500.50"),
                "TRANSFERENCIA",
                "APROVADA",
                "Transferência teste",
                LocalDateTime.of(2026, 4, 9, 10, 30, 0),
                new BigDecimal("5000.00"),
                "12345-6",
                "99999-9"
        );

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // When
        analiseFraudeService.iniciarAnalise(evento);

        // Then
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/engine-rest/process-definition/key/processoAnaliseFraude/start"),
                requestCaptor.capture(),
                eq(String.class)
        );

        HttpEntity capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.getBody()).isInstanceOf(CamundaStartProcessRequestDTO.class);

        CamundaStartProcessRequestDTO body = (CamundaStartProcessRequestDTO) capturedRequest.getBody();
        assertThat(body.businessKey()).isEqualTo("tx-001");

        Map<String, CamundaVariableDTO> variaveis = body.variables();
        assertThat(variaveis).containsKeys(
                "idTransacao",
                "numeroConta",
                "valor",
                "tipo",
                "dataHora",
                "contaOrigem",
                "contaDestino"
        );

        assertThat(variaveis.get("idTransacao").value()).isEqualTo("tx-001");
        assertThat(variaveis.get("idTransacao").type()).isEqualTo("String");

        assertThat(variaveis.get("numeroConta").value()).isEqualTo("12345-6");
        assertThat(variaveis.get("numeroConta").type()).isEqualTo("String");

        assertThat(variaveis.get("valor").value()).isEqualTo(1500.50d);
        assertThat(variaveis.get("valor").type()).isEqualTo("Double");

        assertThat(variaveis.get("tipo").value()).isEqualTo("TRANSFERENCIA");
        assertThat(variaveis.get("tipo").type()).isEqualTo("String");

        assertThat(variaveis.get("dataHora").value()).isEqualTo("2026-04-09T10:30");
        assertThat(variaveis.get("dataHora").type()).isEqualTo("String");

        assertThat(variaveis.get("contaOrigem").value()).isEqualTo("12345-6");
        assertThat(variaveis.get("contaDestino").value()).isEqualTo("99999-9");
    }

    @Test
    @DisplayName("Deve iniciar análise com campos opcionais nulos")
    void deveIniciarAnaliseComCamposOpcionaisNulos() {
        // Given
        TransacaoEventoDTO evento = new TransacaoEventoDTO(
                "tx-002",
                "54321-0",
                new BigDecimal("200.00"),
                "DEBITO",
                "APROVADA",
                "Compra",
                null,
                new BigDecimal("1000.00"),
                null,
                null
        );

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // When
        analiseFraudeService.iniciarAnalise(evento);

        // Then
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/engine-rest/process-definition/key/processoAnaliseFraude/start"),
                requestCaptor.capture(),
                eq(String.class)
        );

        CamundaStartProcessRequestDTO body = (CamundaStartProcessRequestDTO) requestCaptor.getValue().getBody();
        Map<String, CamundaVariableDTO> variaveis = body.variables();

        assertThat(variaveis.get("dataHora").value()).isEqualTo("");
        assertThat(variaveis.get("contaOrigem").value()).isEqualTo("");
        assertThat(variaveis.get("contaDestino").value()).isEqualTo("");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando RestTemplate falhar")
    void naoDeveLancarExcecaoQuandoRestTemplateFalhar() {
        // Given
        TransacaoEventoDTO evento = new TransacaoEventoDTO(
                "tx-003",
                "11111-1",
                new BigDecimal("300.00"),
                "CREDITO",
                "APROVADA",
                "Crédito teste",
                LocalDateTime.now(),
                new BigDecimal("1300.00"),
                "11111-1",
                "22222-2"
        );

        doThrow(new RuntimeException("Erro ao chamar Camunda"))
                .when(restTemplate)
                .postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        // When
        analiseFraudeService.iniciarAnalise(evento);

        // Then
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/engine-rest/process-definition/key/processoAnaliseFraude/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}