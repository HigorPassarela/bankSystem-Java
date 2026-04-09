package br.com.banksystem.transacoes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do SaldoDTO")
class SaldoDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        SaldoDTO dto = new SaldoDTO(
                "12345-6",
                new BigDecimal("1000.00"),
                new BigDecimal("500.00")
        );

        assertThat(dto.numeroConta()).isEqualTo("12345-6");
        assertThat(dto.saldoDisponivel()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(dto.limiteDisponivel()).isEqualTo(new BigDecimal("500.00"));
    }
}