package br.com.banksystem.extratos.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TransacaoDTO")
class TransacaoDTOTest {

    @Test
    @DisplayName("Deve criar record e retornar valores corretamente")
    void deveCriarRecordCorretamente() {
        // Given
        BigDecimal valor = new BigDecimal("500.00");
        BigDecimal saldo = new BigDecimal("1500.00");
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        TransacaoDTO dto = new TransacaoDTO(
                "tx-001",
                "12345-6",
                valor,
                "DEPOSITO",
                "APROVADA",
                "Depósito realizado",
                dataHora,
                saldo
        );

        // Then
        assertThat(dto.idTransacao()).isEqualTo("tx-001");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipo()).isEqualTo("DEPOSITO");
        assertThat(dto.status()).isEqualTo("APROVADA");
        assertThat(dto.descricao()).isEqualTo("Depósito realizado");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.saldoAposTransacao()).isEqualTo(saldo);
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        TransacaoDTO dto = new TransacaoDTO(
                null, null, null, null, null, null, null, null
        );

        // Then
        assertThat(dto.idTransacao()).isNull();
        assertThat(dto.numeroConta()).isNull();
        assertThat(dto.valor()).isNull();
        assertThat(dto.tipo()).isNull();
        assertThat(dto.status()).isNull();
        assertThat(dto.descricao()).isNull();
        assertThat(dto.dataHora()).isNull();
        assertThat(dto.saldoAposTransacao()).isNull();
    }
}