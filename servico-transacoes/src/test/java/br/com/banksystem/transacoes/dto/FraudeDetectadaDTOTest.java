package br.com.banksystem.transacoes.dto;

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
        LocalDateTime dataHora = LocalDateTime.now();
        BigDecimal valor = new BigDecimal("999.99");

        FraudeDetectadaDTO dto = new FraudeDetectadaDTO(
                "tx-002",
                "12345-6",
                valor,
                "DEBITO",
                95,
                "REPROVADA",
                dataHora,
                "12345-6",
                "88888-8"
        );

        assertThat(dto.idTransacao()).isEqualTo("tx-002");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipo()).isEqualTo("DEBITO");
        assertThat(dto.scoreRisco()).isEqualTo(95);
        assertThat(dto.resultadoAntifraude()).isEqualTo("REPROVADA");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.contaOrigem()).isEqualTo("12345-6");
        assertThat(dto.contaDestino()).isEqualTo("88888-8");
    }
}