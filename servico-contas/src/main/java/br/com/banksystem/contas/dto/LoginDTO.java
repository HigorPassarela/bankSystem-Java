package br.com.banksystem.contas.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para autenticação de conta.
 */
public record LoginDTO(
        @NotBlank(message = "Número da conta é obrigatório")
        String numeroConta,

        @NotBlank(message = "Senha é obrigatória")
        String senha
) {}
