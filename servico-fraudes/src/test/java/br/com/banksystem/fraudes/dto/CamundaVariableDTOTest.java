package br.com.banksystem.fraudes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do CamundaVariableDTO")
class CamundaVariableDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente com valor string")
    void deveCriarRecordCorretamenteComValorString() {
        // When
        CamundaVariableDTO dto = new CamundaVariableDTO("tx-001", "String");

        // Then
        assertThat(dto.value()).isEqualTo("tx-001");
        assertThat(dto.type()).isEqualTo("String");
    }

    @Test
    @DisplayName("Deve criar record corretamente com valor numérico")
    void deveCriarRecordCorretamenteComValorNumerico() {
        // When
        CamundaVariableDTO dto = new CamundaVariableDTO(85, "Integer");

        // Then
        assertThat(dto.value()).isEqualTo(85);
        assertThat(dto.type()).isEqualTo("Integer");
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        CamundaVariableDTO dto = new CamundaVariableDTO(null, null);

        // Then
        assertThat(dto.value()).isNull();
        assertThat(dto.type()).isNull();
    }
}