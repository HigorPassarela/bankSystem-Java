package br.com.banksystem.notificacoes.service;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SseEmitterServiceTest {

    private SseEmitterService sseEmitterService;

    @BeforeEach
    void setUp() {
        sseEmitterService = new SseEmitterService();
    }

    @Test
    void deveCriarEmissorComSucesso() {
        // Given
        String numeroConta = "12345";

        // When
        SseEmitter emissor = sseEmitterService.criarEmissor(numeroConta);

        // Then
        assertNotNull(emissor);
    }

    @Test
    void deveEnviarNotificacaoParaEmissorAtivo() {
        // Given
        String numeroConta = "12345";
        NotificacaoDTO notificacao = criarNotificacaoDTO();

        // When
        SseEmitter emissor = sseEmitterService.criarEmissor(numeroConta);
        sseEmitterService.enviarNotificacao(numeroConta, notificacao);

        // Then
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
        assertEquals(notificacao, historico.get(0));
    }

    @Test
    void deveAdicionarNotificacaoAoHistoricoMesmoSemEmissores() {
        // Given
        String numeroConta = "12345";
        NotificacaoDTO notificacao = criarNotificacaoDTO();

        // When
        sseEmitterService.enviarNotificacao(numeroConta, notificacao);

        // Then
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
        assertEquals(notificacao, historico.get(0));
    }

    @Test
    void deveRetornarHistoricoVazioParaContaInexistente() {
        // Given
        String numeroConta = "99999";

        // When
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);

        // Then
        assertTrue(historico.isEmpty());
    }

    @Test
    void devePermitirMultiplosEmissoresParaMesmaConta() {
        // Given
        String numeroConta = "12345";

        // When
        SseEmitter emissor1 = sseEmitterService.criarEmissor(numeroConta);
        SseEmitter emissor2 = sseEmitterService.criarEmissor(numeroConta);

        // Then
        assertNotNull(emissor1);
        assertNotNull(emissor2);
        assertNotSame(emissor1, emissor2);
    }

    @Test
    void deveManterHistoricoSeparadoPorConta() {
        // Given
        String conta1 = "12345";
        String conta2 = "67890";
        NotificacaoDTO notificacao1 = criarNotificacaoDTO("TRANSACAO", "Transferência realizada conta 1");
        NotificacaoDTO notificacao2 = criarNotificacaoDTO("PIX", "PIX recebido conta 2");

        // When
        sseEmitterService.enviarNotificacao(conta1, notificacao1);
        sseEmitterService.enviarNotificacao(conta2, notificacao2);

        // Then
        List<NotificacaoDTO> historico1 = sseEmitterService.obterHistorico(conta1);
        List<NotificacaoDTO> historico2 = sseEmitterService.obterHistorico(conta2);

        assertEquals(1, historico1.size());
        assertEquals(1, historico2.size());
        assertEquals("Transferência realizada conta 1", historico1.get(0).mensagem());
        assertEquals("PIX recebido conta 2", historico2.get(0).mensagem());
        assertEquals("TRANSACAO", historico1.get(0).tipo());
        assertEquals("PIX", historico2.get(0).tipo());
    }

    @Test
    void deveAcumularNotificacaoNoHistorico() {
        // Given
        String numeroConta = "12345";
        NotificacaoDTO notificacao1 = criarNotificacaoDTO("TRANSACAO", "Primeira transação");
        NotificacaoDTO notificacao2 = criarNotificacaoDTO("PIX", "Segunda transação");

        // When
        sseEmitterService.enviarNotificacao(numeroConta, notificacao1);
        sseEmitterService.enviarNotificacao(numeroConta, notificacao2);

        // Then
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(2, historico.size());
        assertEquals("Primeira transação", historico.get(0).mensagem());
        assertEquals("Segunda transação", historico.get(1).mensagem());
    }

    @Test
    void deveGerenciarEmissoresDeContasDiferentes() {
        // Given
        String conta1 = "12345";
        String conta2 = "67890";
        NotificacaoDTO notificacao1 = criarNotificacaoDTO("TRANSACAO", "Notificação conta 1");
        NotificacaoDTO notificacao2 = criarNotificacaoDTO("SALDO", "Notificação conta 2");

        // When
        SseEmitter emissor1 = sseEmitterService.criarEmissor(conta1);
        SseEmitter emissor2 = sseEmitterService.criarEmissor(conta2);

        sseEmitterService.enviarNotificacao(conta1, notificacao1);
        sseEmitterService.enviarNotificacao(conta2, notificacao2);

        // Then
        assertNotNull(emissor1);
        assertNotNull(emissor2);

        List<NotificacaoDTO> historico1 = sseEmitterService.obterHistorico(conta1);
        List<NotificacaoDTO> historico2 = sseEmitterService.obterHistorico(conta2);

        assertEquals(1, historico1.size());
        assertEquals(1, historico2.size());
        assertNotEquals(historico1.get(0).mensagem(), historico2.get(0).mensagem());
    }

    @Test
    void deveManterHistoricoAposRemocaoDeEmissor() {
        // Given
        String numeroConta = "12345";
        NotificacaoDTO notificacao = criarNotificacaoDTO();

        // When
        SseEmitter emissor = sseEmitterService.criarEmissor(numeroConta);
        sseEmitterService.enviarNotificacao(numeroConta, notificacao);
        emissor.complete(); // Simula a remoção do emissor

        // Then
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
        assertEquals(notificacao, historico.get(0));
    }

    @Test
    void devePermitirEnvioDeNotificacaoSemEmissoresAtivos() {
        // Given
        String numeroConta = "12345";
        NotificacaoDTO notificacao = criarNotificacaoDTO();

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            sseEmitterService.enviarNotificacao(numeroConta, notificacao);
        });

        // Verifica se foi adicionado ao histórico
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
    }

    @Test
    void deveSerializarNotificacaoCorretamenteParaJson() {
        // Given
        String numeroConta = "12345";
        Map<String, Object> dados = Map.of(
                "valor", 100.50,
                "contaDestino", "67890"
        );
        NotificacaoDTO notificacao = new NotificacaoDTO(
                "TRANSFERENCIA",
                "Transferência realizada com sucesso",
                dados,
                LocalDateTime.now()
        );

        // When
        SseEmitter emissor = sseEmitterService.criarEmissor(numeroConta);

        // Then - Não deve lançar exceção na serialização
        assertDoesNotThrow(() -> {
            sseEmitterService.enviarNotificacao(numeroConta, notificacao);
        });

        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
        assertEquals("TRANSFERENCIA", historico.get(0).tipo());
        assertEquals("Transferência realizada com sucesso", historico.get(0).mensagem());
        assertNotNull(historico.get(0).dados());
    }

    @Test
    void deveUsarMetodoStaticoCriarCorretamente() {
        // Given
        String tipo = "SALDO";
        String mensagem = "Saldo atualizado";
        Map<String, Object> dados = Map.of("novoSaldo", 1500.00);

        // When
        NotificacaoDTO notificacao = NotificacaoDTO.criar(tipo, mensagem, dados);

        // Then
        assertNotNull(notificacao);
        assertEquals(tipo, notificacao.tipo());
        assertEquals(mensagem, notificacao.mensagem());
        assertEquals(dados, notificacao.dados());
        assertNotNull(notificacao.timestamp());
        assertTrue(notificacao.timestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void deveManterTimestampOriginalDaNotificacao() {
        // Given
        String numeroConta = "12345";
        LocalDateTime timestampEspecifico = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
        NotificacaoDTO notificacao = new NotificacaoDTO(
                "NATAL",
                "Feliz Natal!",
                Map.of("bonus", 100.0),
                timestampEspecifico
        );

        // When
        sseEmitterService.enviarNotificacao(numeroConta, notificacao);

        // Then
        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
        assertEquals(timestampEspecifico, historico.get(0).timestamp());
    }

    @Test
    void devePermitirDadosNulos() {
        // Given
        String numeroConta = "12345";
        NotificacaoDTO notificacao = new NotificacaoDTO(
                "SIMPLES",
                "Mensagem simples sem dados",
                null,
                LocalDateTime.now()
        );

        // When & Then
        assertDoesNotThrow(() -> {
            sseEmitterService.enviarNotificacao(numeroConta, notificacao);
        });

        List<NotificacaoDTO> historico = sseEmitterService.obterHistorico(numeroConta);
        assertEquals(1, historico.size());
        assertNull(historico.get(0).dados());
    }

    // Métodos auxiliares para criação de NotificacaoDTO
    private NotificacaoDTO criarNotificacaoDTO() {
        return criarNotificacaoDTO("TRANSACAO", "Mensagem de teste");
    }

    private NotificacaoDTO criarNotificacaoDTO(String tipo, String mensagem) {
        Map<String, Object> dados = Map.of(
                "valor", 150.75,
                "conta", "12345"
        );
        return NotificacaoDTO.criar(tipo, mensagem, dados);
    }
}