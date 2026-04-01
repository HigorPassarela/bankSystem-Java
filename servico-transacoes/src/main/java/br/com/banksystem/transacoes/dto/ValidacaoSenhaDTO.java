package br.com.banksystem.transacoes.dto;

/**
 * DTO de resposta de validação de senha de transferência.
 */
public record ValidacaoSenhaDTO(boolean sucesso, Object dados, String mensagem, Object timestamp) {}
