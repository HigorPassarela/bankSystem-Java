package br.com.banksystem.fraudes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CamundaDeploymentService")
class CamundaDeploymentServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CamundaDeploymentService camundaDeploymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(camundaDeploymentService, "camundaUrl", "http://localhost:8080/engine-rest");
    }

    @Test
    @DisplayName("Deve realizar deploy do processo com sucesso")
    void deveRealizarDeployDoProcessoComSucesso() {
        // Given
        when(restTemplate.postForEntity(
                eq("http://localhost:8080/engine-rest/deployment/create"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("deploy realizado"));

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        // When
        camundaDeploymentService.deployarProcesso();

        // Then
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/engine-rest/deployment/create"),
                requestCaptor.capture(),
                eq(String.class)
        );

        HttpEntity request = requestCaptor.getValue();
        assertThat(request).isNotNull();
        assertThat(request.getBody()).isInstanceOf(MultiValueMap.class);

        MultiValueMap<String, Object> body = (MultiValueMap<String, Object>) request.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getFirst("deployment-name")).isEqualTo("fraudes-deployment");
        assertThat(body.getFirst("enable-duplicate-filtering")).isEqualTo("true");
        assertThat(body.getFirst("deploy-changed-only")).isEqualTo("true");
        assertThat(body.getFirst("data")).isNotNull();

        assertThat(request.getHeaders().getContentType()).isNotNull();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando ocorrer erro no deploy")
    void naoDeveLancarExcecaoQuandoOcorrerErroNoDeploy() {
        // Given
        doThrow(new RuntimeException("Erro ao conectar com Camunda"))
                .when(restTemplate)
                .postForEntity(
                        eq("http://localhost:8080/engine-rest/deployment/create"),
                        any(HttpEntity.class),
                        eq(String.class)
                );

        // When
        camundaDeploymentService.deployarProcesso();

        // Then
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/engine-rest/deployment/create"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}