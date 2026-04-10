package br.com.banksystem.contas.dto;

import br.com.banksystem.contas.model.enums.StatusConta;

import java.time.LocalDateTime;

/**
 * DTO de exibição do perfil da conta.
 * O campo "status" indica o estado atual da conta:
 * PENDENTE_EMAIL → aguardando verificação de e-mail
 * ATIVA          → conta operacional
 * SUSPENSA       → bloqueada
 * ENCERRADA      → encerrada
 */
public record PerfilContaDTO(
        String numeroConta,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        StatusConta status,
        Boolean ativa,
        Boolean emailVerificado,
        LocalDateTime dataCriacao
) {
}
