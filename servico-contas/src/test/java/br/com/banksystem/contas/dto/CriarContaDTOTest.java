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

@DisplayName("Testes do CriarContaDTO")
class CriarContaDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        CriarContaDTO dto = new CriarContaDTO(
                "João da Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "senha123",
                "1234"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar nome em branco")
    void deveRejeitarNomeEmBranco() {
        CriarContaDTO dto = new CriarContaDTO(
                "",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "senha123",
                "1234"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nomeCompleto"));
    }

    @Test
    @DisplayName("Deve rejeitar CPF inválido")
    void deveRejeitarCpfInvalido() {
        CriarContaDTO dto = new CriarContaDTO(
                "João da Silva",
                "123",
                "joao@email.com",
                "11999999999",
                "senha123",
                "1234"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("cpf"));
    }

    @Test
    @DisplayName("Deve rejeitar email inválido")
    void deveRejeitarEmailInvalido() {
        CriarContaDTO dto = new CriarContaDTO(
                "João da Silva",
                "12345678901",
                "email-invalido",
                "11999999999",
                "senha123",
                "1234"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Deve rejeitar telefone inválido")
    void deveRejeitarTelefoneInvalido() {
        CriarContaDTO dto = new CriarContaDTO(
                "João da Silva",
                "12345678901",
                "joao@email.com",
                "123",
                "senha123",
                "1234"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("telefone"));
    }

    @Test
    @DisplayName("Deve rejeitar senha curta")
    void deveRejeitarSenhaCurta() {
        CriarContaDTO dto = new CriarContaDTO(
                "João da Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "123",
                "1234"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("senha"));
    }

    @Test
    @DisplayName("Deve rejeitar senha de transferência inválida")
    void deveRejeitarSenhaTransferenciaInvalida() {
        CriarContaDTO dto = new CriarContaDTO(
                "João da Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "senha123",
                "12a"
        );

        Set<ConstraintViolation<CriarContaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("senhaTransferencia"));
    }
}