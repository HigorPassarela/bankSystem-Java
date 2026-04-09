package br.com.banksystem.fraudes.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do CamundaStartProcessRequestDTO")
class CamundaStartProcessRequestDTOTest {

    @Test
    @DisplayName("Deve criar record corretamente")
    void deveCriarRecordCorretamente() {
        // Given
        CamundaVariableDTO variable1 = new CamundaVariableDTO("tx-001", "String");
        CamundaVariableDTO variable2 = new CamundaVariableDTO(1500.0, "Double");

        Map<String, CamundaVariableDTO> variables = Map.of(
                "idTransacao", variable1,
                "valor", variable2
        );

        // When
        CamundaStartProcessRequestDTO dto = new CamundaStartProcessRequestDTO("business-key-001", variables);

        // Then
        assertThat(dto.businessKey()).isEqualTo("business-key-001");
        assertThat(dto.variables()).isEqualTo(variables);
        assertThat(dto.variables()).hasSize(2);
        assertThat(dto.variables()).containsKey("idTransacao");
        assertThat(dto.variables()).containsKey("valor");
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        // When
        CamundaStartProcessRequestDTO dto = new CamundaStartProcessRequestDTO(null, null);

        // Then
        assertThat(dto.businessKey()).isNull();
        assertThat(dto.variables()).isNull();
    }
}