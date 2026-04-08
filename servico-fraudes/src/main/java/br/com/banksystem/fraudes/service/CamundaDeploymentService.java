package br.com.banksystem.fraudes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class CamundaDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(CamundaDeploymentService.class);

    private final RestTemplate restTemplate;

    @Value("${servico.camunda.url}")
    private String camundaUrl;

    public CamundaDeploymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deployarProcesso() {
        try {
            Resource bpmn = new ClassPathResource("processos/analise-fraude.bpmn");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("deployment-name", "fraudes-deployment");
            body.add("enable-duplicate-filtering", "true");
            body.add("deploy-changed-only", "true");
            body.add("data", bpmn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    camundaUrl + "/deployment/create",
                    request,
                    String.class
            );

            log.info("Deploy do BPMN realizado no Camunda externo. Resposta: {}", response.getBody());
        } catch (Exception e) {
            log.error("Erro ao fazer deploy do BPMN no Camunda externo: {}", e.getMessage(), e);
        }
    }
}