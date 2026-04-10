package br.com.banksystem.transacoes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para solicitação de depósito em conta.
 * Depósito credita diretamente no saldo disponível (não no limite).
 */
public record DepositoDTO(
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor mínimo de depósito é R$ 0,01")
        @DecimalMax(value = "100000.00", message = "Valor máximo por depósito é R$ 100.000,00")
        BigDecimal valor,

        String descricao
) {
}
