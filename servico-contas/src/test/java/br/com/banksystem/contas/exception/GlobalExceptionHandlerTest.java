package br.com.banksystem.contas.exception;

import br.com.banksystem.contas.dto.RespostaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        // Setup comum se necessário
    }

    @Test
    void tratarContaNaoEncontrada_DeveRetornarNotFoundComMensagemCorreta() {
        // Arrange
        String mensagemErro = "Conta com número 12345 não foi encontrada";
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException(mensagemErro);

        // Act
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarContaNaoEncontrada(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals(mensagemErro, response.getBody().mensagem());
        assertNull(response.getBody().dados());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarContaJaExiste_DeveRetornarConflictComMensagemCorreta() {
        // Arrange
        String mensagemErro = "Conta com número 12345 já existe";
        ContaJaExisteException exception = new ContaJaExisteException(mensagemErro);

        // Act
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarContaJaExiste(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals(mensagemErro, response.getBody().mensagem());
        assertNull(response.getBody().dados());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarCredenciaisInvalidas_DeveRetornarUnauthorizedComMensagemPadrao() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Credenciais inválidas");

        // Act
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarCredenciaisInvalidas(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Número da conta ou senha inválidos", response.getBody().mensagem());
        assertNull(response.getBody().dados());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarValidacao_DeveRetornarBadRequestComErrosDosCampos() {
        // Arrange
        FieldError fieldError1 = new FieldError("objeto", "nome", "Nome é obrigatório");
        FieldError fieldError2 = new FieldError("objeto", "email", "Email deve ser válido");

        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<RespostaDTO<Map<String, String>>> response = globalExceptionHandler.tratarValidacao(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Erro de validação nos campos informados", response.getBody().mensagem());

        Map<String, String> erros = response.getBody().dados();
        assertNotNull(erros);
        assertEquals(2, erros.size());
        assertEquals("Nome é obrigatório", erros.get("nome"));
        assertEquals("Email deve ser válido", erros.get("email"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarValidacao_ComUmCampo_DeveRetornarErroCorreto() {
        // Arrange
        FieldError fieldError = new FieldError("objeto", "cpf", "CPF deve ter 11 dígitos");

        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<RespostaDTO<Map<String, String>>> response = globalExceptionHandler.tratarValidacao(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Erro de validação nos campos informados", response.getBody().mensagem());

        Map<String, String> erros = response.getBody().dados();
        assertNotNull(erros);
        assertEquals(1, erros.size());
        assertEquals("CPF deve ter 11 dígitos", erros.get("cpf"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarExcecaoGeral_DeveRetornarInternalServerErrorComMensagemGenerica() {
        // Arrange
        Exception exception = new RuntimeException("Erro inesperado no sistema");

        // Act
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarExcecaoGeral(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Erro interno no servidor. Tente novamente mais tarde.", response.getBody().mensagem());
        assertNull(response.getBody().dados());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarExcecaoGeral_ComNullPointerException_DeveRetornarInternalServerError() {
        // Arrange
        Exception exception = new NullPointerException("Objeto não pode ser null");

        // Act
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarExcecaoGeral(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Erro interno no servidor. Tente novamente mais tarde.", response.getBody().mensagem());
        assertNull(response.getBody().dados());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarValidacao_ComListaVazia_DeveRetornarMapaVazio() {
        // Arrange
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList());

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<RespostaDTO<Map<String, String>>> response = globalExceptionHandler.tratarValidacao(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Erro de validação nos campos informados", response.getBody().mensagem());

        Map<String, String> erros = response.getBody().dados();
        assertNotNull(erros);
        assertTrue(erros.isEmpty());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void tratarValidacao_DeveVerificarTimestampRecente() {
        // Arrange
        FieldError fieldError = new FieldError("objeto", "nome", "Nome é obrigatório");
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<RespostaDTO<Map<String, String>>> response = globalExceptionHandler.tratarValidacao(exception);

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().timestamp());
        // Verifica se o timestamp é recente (dentro de 1 segundo)
        assertTrue(response.getBody().timestamp().isAfter(java.time.LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void tratarContaNaoEncontrada_DeveVerificarTimestampRecente() {
        // Arrange
        ContaNaoEncontradaException exception = new ContaNaoEncontradaException("Conta não encontrada");

        // Act
        ResponseEntity<RespostaDTO<Void>> response = globalExceptionHandler.tratarContaNaoEncontrada(exception);

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().timestamp());
        // Verifica se o timestamp é recente (dentro de 1 segundo)
        assertTrue(response.getBody().timestamp().isAfter(java.time.LocalDateTime.now().minusSeconds(1)));
    }
}