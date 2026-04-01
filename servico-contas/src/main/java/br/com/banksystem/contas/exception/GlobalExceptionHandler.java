package br.com.banksystem.contas.exception;

import br.com.banksystem.contas.dto.RespostaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Tratamento global de exceções para o Serviço de Contas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<RespostaDTO<Void>> tratarContaNaoEncontrada(ContaNaoEncontradaException ex) {
        log.warn("Conta não encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RespostaDTO.erro(ex.getMessage()));
    }

    @ExceptionHandler(ContaJaExisteException.class)
    public ResponseEntity<RespostaDTO<Void>> tratarContaJaExiste(ContaJaExisteException ex) {
        log.warn("Conflito ao criar conta: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(RespostaDTO.erro(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RespostaDTO<Void>> tratarCredenciaisInvalidas(BadCredentialsException ex) {
        log.warn("Tentativa de login com credenciais inválidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(RespostaDTO.erro("Número da conta ou senha inválidos"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaDTO<Map<String, String>>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensagem = error.getDefaultMessage();
            erros.put(campo, mensagem);
        });
        log.warn("Erro de validação nos campos: {}", erros.keySet());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespostaDTO<>(false, erros, "Erro de validação nos campos informados",
                        java.time.LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaDTO<Void>> tratarExcecaoGeral(Exception ex) {
        log.error("Erro interno no serviço de contas: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespostaDTO.erro("Erro interno no servidor. Tente novamente mais tarde."));
    }
}
