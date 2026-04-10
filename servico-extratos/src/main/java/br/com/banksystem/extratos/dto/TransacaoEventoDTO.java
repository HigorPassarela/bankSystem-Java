package br.com.banksystem.extratos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO do evento Kafka consumido pelo serviço de extratos.
 */
public record TransacaoEventoDTO(
        String idTransacao,
        String numeroConta,
        BigDecimal valor,
        String tipo,
        String status,
        String descricao,
        LocalDateTime dataHora,
        BigDecimal saldoAposTransacao,
        String contaOrigem,
        String contaDestino
) {
}