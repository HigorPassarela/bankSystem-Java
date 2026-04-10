package br.com.banksystem.transacoes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para solicitação de transação de débito.
 */
public record DebitoDTO(
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01")
        BigDecimal valor,

        String descricao
) {
}
