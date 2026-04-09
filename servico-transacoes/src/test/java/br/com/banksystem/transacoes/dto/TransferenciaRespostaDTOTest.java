package br.com.banksystem.transacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TransferenciaRespostaDTO")
class TransferenciaRespostaDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        LocalDateTime dataHora = LocalDateTime.now();

        TransferenciaRespostaDTO dto = new TransferenciaRespostaDTO(
                "tx-005",
                "11111111",
                "22222222",
                new BigDecimal("200.00"),
                "APROVADA",
                new BigDecimal("800.00"),
                dataHora
        );

        assertThat(dto.idTransacao()).isEqualTo("tx-005");
        assertThat(dto.contaOrigem()).isEqualTo("11111111");
        assertThat(dto.contaDestino()).isEqualTo("22222222");
        assertThat(dto.valor()).isEqualTo(new BigDecimal("200.00"));
        assertThat(dto.status()).isEqualTo("APROVADA");
        assertThat(dto.saldoOrigemAtualizado()).isEqualTo(new BigDecimal("800.00"));
        assertThat(dto.dataHora()).isEqualTo(dataHora);
    }
}