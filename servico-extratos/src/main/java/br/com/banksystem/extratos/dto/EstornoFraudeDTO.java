package br.com.banksystem.extratos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO consumido pelo serviço de extratos quando uma transação é estornada por fraude.
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