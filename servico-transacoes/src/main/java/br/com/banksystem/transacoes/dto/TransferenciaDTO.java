package br.com.banksystem.transacoes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para solicitação de transferência entre contas.
 * Requer o PIN de 4 dígitos (senha de transferência) para autorização.
 */
public record TransferenciaDTO(
        @NotBlank(message = "Conta de destino é obrigatória")
        @Pattern(regexp = "\\d{8}", message = "Número de conta deve ter 8 dígitos")
        String contaDestino,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01")
        @DecimalMax(value = "50000.00", message = "Valor máximo por transferência é R$ 50.000,00")
        BigDecimal valor,

        @NotBlank(message = "Senha de transferência (PIN 4 dígitos) é obrigatória")
        @Pattern(regexp = "\\d{4}", message = "Senha de transferência deve ter exatamente 4 dígitos")
        String senhaTransferencia,

        String descricao
) {
}
