package br.com.banksystem.extratos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO do evento Kafka de transação (produzido pelo servico-transacoes).
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
