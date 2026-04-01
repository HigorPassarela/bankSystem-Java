package br.com.banksystem.transacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de resposta de uma transação processada.
 */
public record TransacaoRespostaDTO(
        String idTransacao,
        String numeroConta,
        BigDecimal valor,
        String tipo,
        String status,
        BigDecimal saldoAtualizado,
        LocalDateTime dataHora
) {}
