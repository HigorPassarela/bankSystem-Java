package br.com.banksystem.notificacoes.dto;
import java.time.LocalDateTime;
public record NotificacaoDTO(String tipo, String mensagem, Object dados, LocalDateTime timestamp) {
    public static NotificacaoDTO criar(String tipo, String mensagem, Object dados) {
        return new NotificacaoDTO(tipo, mensagem, dados, LocalDateTime.now());
    }
}
