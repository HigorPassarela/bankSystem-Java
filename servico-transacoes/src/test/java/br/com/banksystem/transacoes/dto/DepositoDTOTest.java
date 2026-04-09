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

@DisplayName("Testes do DepositoDTO")
class DepositoDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        DepositoDTO dto = new DepositoDTO(new BigDecimal("500.00"), "Depósito em caixa");

        Set<ConstraintViolation<DepositoDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar valor nulo")
    void deveRejeitarValorNulo() {
        DepositoDTO dto = new DepositoDTO(null, "Depósito em caixa");

        Set<ConstraintViolation<DepositoDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }

    @Test
    @DisplayName("Deve rejeitar valor menor que o mínimo")
    void deveRejeitarValorMenorQueMinimo() {
        DepositoDTO dto = new DepositoDTO(new BigDecimal("0.00"), "Depósito em caixa");

        Set<ConstraintViolation<DepositoDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }

    @Test
    @DisplayName("Deve rejeitar valor maior que o máximo")
    void deveRejeitarValorMaiorQueMaximo() {
        DepositoDTO dto = new DepositoDTO(new BigDecimal("100000.01"), "Depósito em caixa");

        Set<ConstraintViolation<DepositoDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }
}