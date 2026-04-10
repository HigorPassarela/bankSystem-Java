package br.com.banksystem.contas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para atualização de dados da conta.
 */
public record AtualizarContaDTO(
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String nomeCompleto,

        @Email(message = "E-mail inválido")
        String email,

        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos")
        String telefone,

        @Size(min = 6, max = 50, message = "Senha deve ter entre 6 e 50 caracteres")
        String novaSenha
) {
}
