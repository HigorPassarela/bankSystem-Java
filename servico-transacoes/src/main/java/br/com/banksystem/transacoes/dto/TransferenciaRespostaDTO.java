package br.com.banksystem.transacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de resposta de uma transferência processada.
 */
public record TransferenciaRespostaDTO(
        String idTransacao,
        String contaOrigem,
        String contaDestino,
        BigDecimal valor,
        String status,
        BigDecimal saldoOrigemAtualizado,
        LocalDateTime dataHora
) {}
