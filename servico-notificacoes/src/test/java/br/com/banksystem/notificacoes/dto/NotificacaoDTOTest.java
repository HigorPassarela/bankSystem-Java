package br.com.banksystem.notificacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do NotificacaoDTO")
class NotificacaoDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, Object> dados = Map.of("id", "123");

        // When
        NotificacaoDTO dto = new NotificacaoDTO(
                "INFO",
                "Notificação enviada",
                dados,
                timestamp
        );

        // Then
        assertThat(dto.tipo()).isEqualTo("INFO");
        assertThat(dto.mensagem()).isEqualTo("Notificação enviada");
        assertThat(dto.dados()).isEqualTo(dados);
        assertThat(dto.timestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Deve criar notificação com método estático criar")
    void deveCriarNotificacaoComMetodoEstaticoCriar() {
        // Given
        Map<String, Object> dados = Map.of("status", "ok");

        // When
        NotificacaoDTO dto = NotificacaoDTO.criar("SUCESSO", "Operação concluída", dados);

        // Then
        assertThat(dto.tipo()).isEqualTo("SUCESSO");
        assertThat(dto.mensagem()).isEqualTo("Operação concluída");
        assertThat(dto.dados()).isEqualTo(dados);
        assertThat(dto.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        NotificacaoDTO dto = new NotificacaoDTO(null, null, null, null);

        // Then
        assertThat(dto.tipo()).isNull();
        assertThat(dto.mensagem()).isNull();
        assertThat(dto.dados()).isNull();
        assertThat(dto.timestamp()).isNull();
    }
}