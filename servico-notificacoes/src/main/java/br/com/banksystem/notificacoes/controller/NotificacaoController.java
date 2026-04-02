package br.com.banksystem.notificacoes.controller;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.dto.RespostaDTO;
import br.com.banksystem.notificacoes.security.JwtUtil;
import br.com.banksystem.notificacoes.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Controller para notificações em tempo real via SSE.
 */
@RestController
@RequestMapping("/api/notificacoes")
//@CrossOrigin(
//        originPatterns = {"http://localhost:*", "https://*.lovable.app"},
//        allowCredentials = "true",
//        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
//        allowedHeaders = "*"
//)
@Tag(name = "Notificações", description = "Endpoints de notificações em tempo real via SSE")
public class NotificacaoController {

    private final SseEmitterService sseEmitterService;
    private final JwtUtil jwtUtil;

    public NotificacaoController(SseEmitterService sseEmitterService, JwtUtil jwtUtil) {
        this.sseEmitterService = sseEmitterService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Conectar ao stream SSE de notificações",
            description = "Estabelece conexão Server-Sent Events para receber notificações em tempo real. " +
                    "Token JWT deve ser passado como query param (?token=...) pois SSE não suporta " +
                    "headers Authorization no navegador."
    )
    public SseEmitter conectarSse(@RequestParam String token) {
        if (!jwtUtil.validarToken(token)) {
            throw new SecurityException("Token JWT inválido ou expirado");
        }

        String numeroConta = jwtUtil.extrairNumeroConta(token);
        return sseEmitterService.criarEmissor(numeroConta);
    }

    @GetMapping("/historico")
    @Operation(
            summary = "Obter histórico de notificações da sessão",
            description = "Retorna as notificações enviadas durante a sessão ativa do usuário",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RespostaDTO<List<NotificacaoDTO>>> historico(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificacaoDTO> notificacoes = sseEmitterService.obterHistorico(userDetails.getUsername());
        return ResponseEntity.ok(RespostaDTO.sucesso(notificacoes,
                "Histórico de " + notificacoes.size() + " notificação(ões) obtido com sucesso"));
    }
}