package br.com.banksystem.fraudes.dto;

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
        BigDecimal valor = new BigDecimal("250.50");
        BigDecimal saldo = new BigDecimal("1250.75");
        LocalDateTime dataHora = LocalDateTime.now();

        // When
        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                "tx-002",
                "54321-0",
                valor,
                "DEBITO",
                "APROVADA",
                "Pagamento realizado",
                dataHora,
                saldo,
                "54321-0",
                "88888-8"
        );

        // Then
        assertThat(dto.idTransacao()).isEqualTo("tx-002");
        assertThat(dto.numeroConta()).isEqualTo("54321-0");
        assertThat(dto.valor()).isEqualTo(valor);
        assertThat(dto.tipo()).isEqualTo("DEBITO");
        assertThat(dto.status()).isEqualTo("APROVADA");
        assertThat(dto.descricao()).isEqualTo("Pagamento realizado");
        assertThat(dto.dataHora()).isEqualTo(dataHora);
        assertThat(dto.saldoAposTransacao()).isEqualTo(saldo);
        assertThat(dto.contaOrigem()).isEqualTo("54321-0");
        assertThat(dto.contaDestino()).isEqualTo("88888-8");
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