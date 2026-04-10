package br.com.banksystem.notificacoes.dto;

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
        // Given
        BigDecimal valor = new BigDecimal("500.00");
        BigDecimal saldo = new BigDecimal("1500.00");
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                "tx-001",
                "12345-6",
                valor,
                "DEPOSITO",
                "APROVADA",
                "Depósito realizado",
                dataHora,
                saldo,
                "12345-6",
                "99999-9"
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
        assertThat(dto.contaOrigem()).isEqualTo("12345-6");
        assertThat(dto.contaDestino()).isEqualTo("99999-9");
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