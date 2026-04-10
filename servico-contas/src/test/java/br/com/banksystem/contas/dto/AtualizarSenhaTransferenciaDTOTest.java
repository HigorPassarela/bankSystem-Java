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

@DisplayName("Testes do AtualizarSenhaTransferenciaDTO")
class AtualizarSenhaTransferenciaDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve aceitar DTO válido")
    void deveAceitarDtoValido() {
        AtualizarSenhaTransferenciaDTO dto = new AtualizarSenhaTransferenciaDTO("1234", "5678");

        Set<ConstraintViolation<AtualizarSenhaTransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar senha atual em branco")
    void deveRejeitarSenhaAtualEmBranco() {
        AtualizarSenhaTransferenciaDTO dto = new AtualizarSenhaTransferenciaDTO("", "5678");

        Set<ConstraintViolation<AtualizarSenhaTransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("senhaAtual"));
    }

    @Test
    @DisplayName("Deve rejeitar nova senha em branco")
    void deveRejeitarNovaSenhaEmBranco() {
        AtualizarSenhaTransferenciaDTO dto = new AtualizarSenhaTransferenciaDTO("1234", "");

        Set<ConstraintViolation<AtualizarSenhaTransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("novaSenha"));
    }

    @Test
    @DisplayName("Deve rejeitar nova senha fora do padrão de 4 dígitos")
    void deveRejeitarNovaSenhaForaDoPadrao() {
        AtualizarSenhaTransferenciaDTO dto = new AtualizarSenhaTransferenciaDTO("1234", "12a");

        Set<ConstraintViolation<AtualizarSenhaTransferenciaDTO>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("novaSenha"));
    }
}