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

@DisplayName("Testes do AtualizarContaDTO")
class AtualizarContaDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        AtualizarContaDTO dto = new AtualizarContaDTO(
                "João da Silva",
                "joao@email.com",
                "11999999999",
                "senha123"
        );

        Set<ConstraintViolation<AtualizarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar nome com menos de 3 caracteres")
    void deveRejeitarNomeCurto() {
        AtualizarContaDTO dto = new AtualizarContaDTO(
                "Jo",
                "joao@email.com",
                "11999999999",
                "senha123"
        );

        Set<ConstraintViolation<AtualizarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nomeCompleto"));
    }

    @Test
    @DisplayName("Deve rejeitar email inválido")
    void deveRejeitarEmailInvalido() {
        AtualizarContaDTO dto = new AtualizarContaDTO(
                "João da Silva",
                "email-invalido",
                "11999999999",
                "senha123"
        );

        Set<ConstraintViolation<AtualizarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Deve rejeitar telefone inválido")
    void deveRejeitarTelefoneInvalido() {
        AtualizarContaDTO dto = new AtualizarContaDTO(
                "João da Silva",
                "joao@email.com",
                "123",
                "senha123"
        );

        Set<ConstraintViolation<AtualizarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("telefone"));
    }

    @Test
    @DisplayName("Deve rejeitar nova senha com menos de 6 caracteres")
    void deveRejeitarNovaSenhaCurta() {
        AtualizarContaDTO dto = new AtualizarContaDTO(
                "João da Silva",
                "joao@email.com",
                "11999999999",
                "123"
        );

        Set<ConstraintViolation<AtualizarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("novaSenha"));
    }

    @Test
    @DisplayName("Deve permitir campos nulos")
    void devePermitirCamposNulos() {
        AtualizarContaDTO dto = new AtualizarContaDTO(null, null, null, null);

        Set<ConstraintViolation<AtualizarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}