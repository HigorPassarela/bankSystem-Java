package br.com.banksystem.transacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO publicado quando uma transação é estornada por suspeita de fraude.
 */
public record EstornoFraudeDTO(
        String idTransacaoOriginal,
        String numeroConta,
        BigDecimal valor,
        String tipoOriginal,
        String motivo,
        LocalDateTime dataHora,
        String contaOrigem,
        String contaDestino
) {}