package br.com.banksystem.notificacoes.controller;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.dto.RespostaDTO;
import br.com.banksystem.notificacoes.security.JwtUtil;
import br.com.banksystem.notificacoes.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Controller para notificações em tempo real via SSE.
 */
@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Endpoints de notificações em tempo real via SSE")
public class NotificacaoController {

    private final SseEmitterService sseEmitterService;
    private final JwtUtil jwtUtil;

    public NotificacaoController(SseEmitterService sseEmitterService, JwtUtil jwtUtil) {
        this.sseEmitterService = sseEmitterService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Endpoint SSE para notificações em tempo real (token via query param)")
    public SseEmitter conectarSse(@RequestParam String token) {
        if (!jwtUtil.validarToken(token)) {
            throw new SecurityException("Token inválido");
        }
        String numeroConta = jwtUtil.extrairNumeroConta(token);
        return sseEmitterService.criarEmissor(numeroConta);
    }

    @GetMapping("/historico")
    @Operation(summary = "Histórico de notificações (placeholder para persistência futura)")
    public ResponseEntity<RespostaDTO<List<NotificacaoDTO>>> historico() {
        return ResponseEntity.ok(RespostaDTO.sucesso(List.of(), "Histórico obtido com sucesso"));
    }
}
