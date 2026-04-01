package br.com.banksystem.transacoes.dto;

import java.time.LocalDateTime;

/** DTO de resposta padronizada. */
public record RespostaDTO<T>(
        boolean sucesso,
        T dados,
        String mensagem,
        LocalDateTime timestamp
) {
    public static <T> RespostaDTO<T> sucesso(T dados, String mensagem) {
        return new RespostaDTO<>(true, dados, mensagem, LocalDateTime.now());
    }
    public static <T> RespostaDTO<T> erro(String mensagem) {
        return new RespostaDTO<>(false, null, mensagem, LocalDateTime.now());
    }
}
