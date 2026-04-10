package br.com.banksystem.transacoes.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do DebitoDTO")
class DebitoDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        DebitoDTO dto = new DebitoDTO(new BigDecimal("50.00"), "Pagamento");

        Set<ConstraintViolation<DebitoDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar valor nulo")
    void deveRejeitarValorNulo() {
        DebitoDTO dto = new DebitoDTO(null, "Pagamento");

        Set<ConstraintViolation<DebitoDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }

    @Test
    @DisplayName("Deve rejeitar valor menor que o mínimo")
    void deveRejeitarValorMenorQueMinimo() {
        DebitoDTO dto = new DebitoDTO(new BigDecimal("0.00"), "Pagamento");

        Set<ConstraintViolation<DebitoDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }
}