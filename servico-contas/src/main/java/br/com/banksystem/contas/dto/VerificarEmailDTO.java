package br.com.banksystem.contas.dto;

/**
 * DTO de resposta de verificação de e-mail.
 */
public record VerificarEmailDTO(
        String mensagem,
        boolean verificado
) {
}
