package br.com.banksystem.contas.dto;

/**
 * DTO de resposta de autenticação com token JWT.
 */
public record TokenDTO(
        String token,
        String tipo,
        String numeroConta,
        String nomeCompleto,
        long expiracaoMs
) {
}
