package br.com.banksystem.transacoes.dto;

import java.math.BigDecimal;

/**
 * DTO de resposta de saldo e limite da conta.
 */
public record SaldoDTO(
        String numeroConta,
        BigDecimal saldoDisponivel,
        BigDecimal limiteDisponivel
) {
}
