package br.com.banksystem.notificacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do EstornoFraudeDTO")
class EstornoFraudeDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        // Given
        BigDecimal valor = new BigDecimal("250.00");
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        EstornoFraudeDTO dto = new EstornoFraudeDTO(
                "tx-original-001",
                "12345-6",
                valor,
                "TRANSFERENCIA_SAIDA",
                "Fraude detectada",
                dataHora,
                "12345-6",
                "99999-9"
        );

        // Then
        assertThat(dto.idTransacaoOriginal()).isEqualTo("tx-original-001");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipoOriginal()).isEqualTo("TRANSFERENCIA_SAIDA");
        assertThat(dto.motivo()).isEqualTo("Fraude detectada");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.contaOrigem()).isEqualTo("12345-6");
        assertThat(dto.contaDestino()).isEqualTo("99999-9");
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        EstornoFraudeDTO dto = new EstornoFraudeDTO(
                null, null, null, null, null, null, null, null
        );

        // Then
        assertThat(dto.idTransacaoOriginal()).isNull();
        assertThat(dto.numeroConta()).isNull();
        assertThat(dto.valor()).isNull();
        assertThat(dto.tipoOriginal()).isNull();
        assertThat(dto.motivo()).isNull();
        assertThat(dto.dataHora()).isNull();
        assertThat(dto.contaOrigem()).isNull();
        assertThat(dto.contaDestino()).isNull();
    }
}