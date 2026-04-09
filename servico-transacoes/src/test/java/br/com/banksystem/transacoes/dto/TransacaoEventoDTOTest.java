package br.com.banksystem.transacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TransacaoEventoDTO")
class TransacaoEventoDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        LocalDateTime dataHora = LocalDateTime.now();

        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                "tx-003",
                "12345-6",
                new BigDecimal("150.00"),
                "DEPOSITO",
                "APROVADA",
                "Depósito recebido",
                dataHora,
                new BigDecimal("1150.00"),
                null,
                null
        );

        assertThat(dto.idTransacao()).isEqualTo("tx-003");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(new BigDecimal("150.00"));
        assertThat(dto.tipo()).isEqualTo("DEPOSITO");
        assertThat(dto.status()).isEqualTo("APROVADA");
        assertThat(dto.descricao()).isEqualTo("Depósito recebido");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.saldoAposTransacao()).isEqualTo(new BigDecimal("1150.00"));
        assertThat(dto.contaOrigem()).isNull();
        assertThat(dto.contaDestino()).isNull();
    }
}