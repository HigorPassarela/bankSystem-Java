package br.com.banksystem.transacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO do evento Kafka publicado após cada transação processada.
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
