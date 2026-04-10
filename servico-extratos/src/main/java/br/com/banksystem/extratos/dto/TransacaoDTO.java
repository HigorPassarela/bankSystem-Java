package br.com.banksystem.extratos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de leitura de uma transação no extrato.
 */
public record TransacaoDTO(
        String idTransacao,
        String numeroConta,
        BigDecimal valor,
        String tipo,
        String status,
        String descricao,
        LocalDateTime dataHora,
        BigDecimal saldoAposTransacao
) {
}
