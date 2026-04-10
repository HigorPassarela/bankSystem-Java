package br.com.banksystem.fraudes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO publicado quando uma transação é marcada como suspeita de fraude.
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