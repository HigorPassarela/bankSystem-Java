package br.com.banksystem.fraudes.dto;

/**
 * DTO de variável no formato esperado pela API REST do Camunda.
 */
public record CamundaVariableDTO(
        Object value,
        String type
) {}