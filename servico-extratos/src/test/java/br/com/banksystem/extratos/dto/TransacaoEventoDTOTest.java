package br.com.banksystem.extratos.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TransacaoEventoDTO")
class TransacaoEventoDTOTest {

    @Test
    @DisplayName("Deve criar record e retornar valores corretamente")
    void deveCriarRecordCorretamente() {
        // Given
        BigDecimal valor = new BigDecimal("300.00");
        BigDecimal saldo = new BigDecimal("1200.00");
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                "tx-002",
                "12345-6",
                valor,
                "TRANSFERENCIA_ENTRADA",
                "APROVADA",
                "Transferência recebida",
                dataHora,
                saldo,
                "98765-4",
                "12345-6"
        );

        // Then
        assertThat(dto.idTransacao()).isEqualTo("tx-002");
        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipo()).isEqualTo("TRANSFERENCIA_ENTRADA");
        assertThat(dto.status()).isEqualTo("APROVADA");
        assertThat(dto.descricao()).isEqualTo("Transferência recebida");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.saldoAposTransacao()).isEqualTo(saldo);
        assertThat(dto.contaOrigem()).isEqualTo("98765-4");
        assertThat(dto.contaDestino()).isEqualTo("12345-6");
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                null, null, null, null, null, null, null, null, null, null
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
        assertThat(dto.contaOrigem()).isNull();
        assertThat(dto.contaDestino()).isNull();
    }
}