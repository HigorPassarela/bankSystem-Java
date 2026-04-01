package br.com.banksystem.transacoes.exception;

import br.com.banksystem.transacoes.dto.RespostaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<RespostaDTO<Void>> tratarSaldoInsuficiente(SaldoInsuficienteException ex) {
        log.warn("Saldo insuficiente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(RespostaDTO.erro(ex.getMessage()));
    }

    @ExceptionHandler(TransferenciaInvalidaException.class)
    public ResponseEntity<RespostaDTO<Void>> tratarTransferenciaInvalida(TransferenciaInvalidaException ex) {
        log.warn("Transferência inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RespostaDTO.erro(ex.getMessage()));
    }

    @ExceptionHandler(ContaInativaException.class)
    public ResponseEntity<RespostaDTO<Void>> tratarContaInativa(ContaInativaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RespostaDTO.erro(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaDTO<Map<String, String>>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            erros.put(campo, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespostaDTO<>(false, erros, "Erro de validação", java.time.LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaDTO<Void>> tratarGeral(Exception ex) {
        log.error("Erro interno: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RespostaDTO.erro("Erro interno no servidor"));
    }
}
