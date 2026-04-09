package br.com.banksystem.fraudes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do FraudeDetectadaDTO")
class FraudeDetectadaDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        // Given
        BigDecimal valor = new BigDecimal("9999.99");
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        FraudeDetectadaDTO dto = new FraudeDetectadaDTO(
                "tx-001",
                "12345-6",
                valor,
                "TRANSFERENCIA",
                95,
                "SUSPEITA_FRAUDE",
                dataHora,
                "12345-6",
                "99999-9"
        );

        // Then
        assertThat(dto.idTransacao()).isEqualTo("tx-001");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipo()).isEqualTo("TRANSFERENCIA");
        assertThat(dto.scoreRisco()).isEqualTo(95);
        assertThat(dto.resultadoAntifraude()).isEqualTo("SUSPEITA_FRAUDE");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.contaOrigem()).isEqualTo("12345-6");
        assertThat(dto.contaDestino()).isEqualTo("99999-9");
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        FraudeDetectadaDTO dto = new FraudeDetectadaDTO(
                null, null, null, null, null, null, null, null, null
        );

        // Then
        assertThat(dto.idTransacao()).isNull();
        assertThat(dto.numeroConta()).isNull();
        assertThat(dto.valor()).isNull();
        assertThat(dto.tipo()).isNull();
        assertThat(dto.scoreRisco()).isNull();
        assertThat(dto.resultadoAntifraude()).isNull();
        assertThat(dto.dataHora()).isNull();
        assertThat(dto.contaOrigem()).isNull();
        assertThat(dto.contaDestino()).isNull();
    }
}