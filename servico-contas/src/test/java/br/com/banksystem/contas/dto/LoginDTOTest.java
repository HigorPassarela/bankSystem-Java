package br.com.banksystem.contas.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do LoginDTO")
class LoginDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        LoginDTO dto = new LoginDTO("12345-6", "senha123");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar número da conta em branco")
    void deveRejeitarNumeroContaEmBranco() {
        LoginDTO dto = new LoginDTO("", "senha123");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("numeroConta"));
    }

    @Test
    @DisplayName("Deve rejeitar senha em branco")
    void deveRejeitarSenhaEmBranco() {
        LoginDTO dto = new LoginDTO("12345-6", "");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("senha"));
    }
}