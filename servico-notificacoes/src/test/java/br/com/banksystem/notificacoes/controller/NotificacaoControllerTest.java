package br.com.banksystem.notificacoes.controller;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import br.com.banksystem.notificacoes.dto.RespostaDTO;
import br.com.banksystem.notificacoes.security.JwtUtil;
import br.com.banksystem.notificacoes.service.SseEmitterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificacaoController")
class NotificacaoControllerTest {

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private JwtUtil jwtUtil;

    private NotificacaoController notificacaoController;

    @BeforeEach
    void setUp() {
        notificacaoController = new NotificacaoController(sseEmitterService, jwtUtil);
    }

    @Test
    @DisplayName("Deve conectar SSE com token válido")
    void deveConectarSseComTokenValido() {
        // Given
        String token = "jwt-token-valido";
        String numeroConta = "12345-6";
        SseEmitter emitter = new SseEmitter();

        when(jwtUtil.validarToken(token)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(token)).thenReturn(numeroConta);
        when(sseEmitterService.criarEmissor(numeroConta)).thenReturn(emitter);

        // When
        SseEmitter resultado = notificacaoController.conectarSse(token);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEqualTo(emitter);

        verify(jwtUtil).validarToken(token);
        verify(jwtUtil).extrairNumeroConta(token);
        verify(sseEmitterService).criarEmissor(numeroConta);
    }

    @Test
    @DisplayName("Deve lançar exceção ao conectar SSE com token inválido")
    void deveLancarExcecaoAoConectarSseComTokenInvalido() {
        // Given
        String token = "jwt-token-invalido";
        when(jwtUtil.validarToken(token)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> notificacaoController.conectarSse(token))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Token JWT inválido ou expirado");

        verify(jwtUtil).validarToken(token);
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(sseEmitterService, never()).criarEmissor(anyString());
    }

    @Test
    @DisplayName("Deve retornar histórico de notificações com sucesso")
    void deveRetornarHistoricoDeNotificacoesComSucesso() {
        // Given
        UserDetails userDetails = User.withUsername("12345-6")
                .password("senha")
                .authorities(List.of())
                .build();

        List<NotificacaoDTO> notificacoes = List.of(
                new NotificacaoDTO("Mensagem 1", "INFO", Map.of("chave", "valor1"), LocalDateTime.now()),
                new NotificacaoDTO("Mensagem 2", "SUCESSO", Map.of("chave", "valor2"), LocalDateTime.now())
        );

        when(sseEmitterService.obterHistorico("12345-6")).thenReturn(notificacoes);

        // When
        ResponseEntity<RespostaDTO<List<NotificacaoDTO>>> response =
                notificacaoController.historico(userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isTrue();
        assertThat(response.getBody().dados()).hasSize(2);
        assertThat(response.getBody().mensagem())
                .isEqualTo("Histórico de 2 notificação(ões) obtido com sucesso");
        assertThat(response.getBody().timestamp()).isNotNull();

        verify(sseEmitterService).obterHistorico("12345-6");
    }

    @Test
    @DisplayName("Deve retornar histórico vazio com mensagem correta")
    void deveRetornarHistoricoVazioComMensagemCorreta() {
        // Given
        UserDetails userDetails = User.withUsername("99999-9")
                .password("senha")
                .authorities(List.of())
                .build();

        List<NotificacaoDTO> notificacoes = List.of();

        when(sseEmitterService.obterHistorico("99999-9")).thenReturn(notificacoes);

        // When
        ResponseEntity<RespostaDTO<List<NotificacaoDTO>>> response =
                notificacaoController.historico(userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sucesso()).isTrue();
        assertThat(response.getBody().dados()).isEmpty();
        assertThat(response.getBody().mensagem())
                .isEqualTo("Histórico de 0 notificação(ões) obtido com sucesso");

        verify(sseEmitterService).obterHistorico("99999-9");
    }
}