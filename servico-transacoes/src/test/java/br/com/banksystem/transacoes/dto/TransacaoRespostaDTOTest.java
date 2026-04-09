package br.com.banksystem.transacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TransacaoRespostaDTO")
class TransacaoRespostaDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        LocalDateTime dataHora = LocalDateTime.now();

        TransacaoRespostaDTO dto = new TransacaoRespostaDTO(
                "tx-004",
                "12345-6",
                new BigDecimal("80.00"),
                "DEBITO",
                "APROVADA",
                new BigDecimal("920.00"),
                dataHora
        );

        assertThat(dto.idTransacao()).isEqualTo("tx-004");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(new BigDecimal("80.00"));
        assertThat(dto.tipo()).isEqualTo("DEBITO");
        assertThat(dto.status()).isEqualTo("APROVADA");
        assertThat(dto.saldoAtualizado()).isEqualTo(new BigDecimal("920.00"));
        assertThat(dto.dataHora()).isEqualTo(dataHora);
    }
}