package br.com.banksystem.contas.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para criação de nova conta bancária.
 * senhaTransferencia: PIN de 4 dígitos exclusivo para transferências.
 */
public record CriarContaDTO(
        @NotBlank(message = "Nome completo é obrigatório")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String nomeCompleto,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
        String cpf,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos")
        String telefone,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 50, message = "Senha deve ter entre 6 e 50 caracteres")
        String senha,

        @NotBlank(message = "Senha de transferência é obrigatória")
        @Pattern(regexp = "\\d{4}", message = "Senha de transferência deve ter exatamente 4 dígitos numéricos")
        String senhaTransferencia
) {}
