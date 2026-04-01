package br.com.banksystem.contas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para atualizar a senha de transferência (PIN de 4 dígitos).
 */
public record AtualizarSenhaTransferenciaDTO(
        @NotBlank(message = "Senha atual de transferência é obrigatória")
        String senhaAtual,

        @NotBlank(message = "Nova senha de transferência é obrigatória")
        @Pattern(regexp = "\\d{4}", message = "Nova senha deve ter exatamente 4 dígitos numéricos")
        String novaSenha
) {}
