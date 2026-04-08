package br.com.banksystem.fraudes.dto;

import java.util.Map;

/**
 * DTO de requisição para iniciar processo via REST do Camunda.
 */
public record CamundaStartProcessRequestDTO(
        String businessKey,
        Map<String, CamundaVariableDTO> variables
) {}