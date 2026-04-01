package br.com.banksystem.notificacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO do evento Kafka de transação.
 */
public record TransacaoEventoDTO(
        String idTransacao,
        String numeroConta,
        BigDecimal valor,
        String tipo,
        String status,
        String descricao,
        LocalDateTime dataHora,
        BigDecimal saldoAposTransacao
) {}
