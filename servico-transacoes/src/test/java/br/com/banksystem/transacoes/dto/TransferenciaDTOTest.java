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

@DisplayName("Testes do TransferenciaDTO")
class TransferenciaDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "12345678",
                new BigDecimal("100.00"),
                "1234",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar conta destino em branco")
    void deveRejeitarContaDestinoEmBranco() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "",
                new BigDecimal("100.00"),
                "1234",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("contaDestino"));
    }

    @Test
    @DisplayName("Deve rejeitar conta destino fora do padrão")
    void deveRejeitarContaDestinoForaDoPadrao() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "123",
                new BigDecimal("100.00"),
                "1234",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("contaDestino"));
    }

    @Test
    @DisplayName("Deve rejeitar valor nulo")
    void deveRejeitarValorNulo() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "12345678",
                null,
                "1234",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }

    @Test
    @DisplayName("Deve rejeitar valor abaixo do mínimo")
    void deveRejeitarValorAbaixoDoMinimo() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "12345678",
                new BigDecimal("0.00"),
                "1234",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }

    @Test
    @DisplayName("Deve rejeitar valor acima do máximo")
    void deveRejeitarValorAcimaDoMaximo() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "12345678",
                new BigDecimal("50000.01"),
                "1234",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("valor"));
    }

    @Test
    @DisplayName("Deve rejeitar senha de transferência em branco")
    void deveRejeitarSenhaTransferenciaEmBranco() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "12345678",
                new BigDecimal("100.00"),
                "",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("senhaTransferencia"));
    }

    @Test
    @DisplayName("Deve rejeitar senha de transferência fora do padrão")
    void deveRejeitarSenhaTransferenciaForaDoPadrao() {
        TransferenciaDTO dto = new TransferenciaDTO(
                "12345678",
                new BigDecimal("100.00"),
                "12a",
                "Transferência teste"
        );

        Set<ConstraintViolation<TransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("senhaTransferencia"));
    }
}