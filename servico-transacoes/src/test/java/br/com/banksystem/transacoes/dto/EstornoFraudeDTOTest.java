package br.com.banksystem.transacoes.dto;

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
        LocalDateTime dataHora = LocalDateTime.now();
        BigDecimal valor = new BigDecimal("300.00");

        EstornoFraudeDTO dto = new EstornoFraudeDTO(
                "tx-001",
                "12345-6",
                valor,
                "TRANSFERENCIA_SAIDA",
                "Fraude detectada",
                dataHora,
                "12345-6",
                "99999-9"
        );

        assertThat(dto.idTransacaoOriginal()).isEqualTo("tx-001");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipoOriginal()).isEqualTo("TRANSFERENCIA_SAIDA");
        assertThat(dto.motivo()).isEqualTo("Fraude detectada");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.contaOrigem()).isEqualTo("12345-6");
        assertThat(dto.contaDestino()).isEqualTo("99999-9");
    }
}