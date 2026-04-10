package br.com.banksystem.transacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO consumido pelo serviço de transações quando uma fraude é detectada.
 */
public record FraudeDetectadaDTO(
        String idTransacao,
        String numeroConta,
        BigDecimal valor,
        String tipo,
        Integer scoreRisco,
        String resultadoAntifraude,
        LocalDateTime dataHora,
        String contaOrigem,
        String contaDestino
) {
}