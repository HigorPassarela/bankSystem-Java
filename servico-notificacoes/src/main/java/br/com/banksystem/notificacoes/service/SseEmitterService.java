package br.com.banksystem.notificacoes.service;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia os emissores SSE por número de conta para notificações em tempo real.
 */
@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private final Map<String, List<SseEmitter>> emissoresPorConta = new ConcurrentHashMap<>();
    private final Map<String, List<NotificacaoDTO>> historicoPorConta = new ConcurrentHashMap<>(); // ← ADICIONADO
    private final ObjectMapper objectMapper;

    public SseEmitterService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public SseEmitter criarEmissor(String numeroConta) {
        SseEmitter emissor = new SseEmitter(Long.MAX_VALUE);
        emissoresPorConta.computeIfAbsent(numeroConta, k -> new ArrayList<>()).add(emissor);

        emissor.onCompletion(() -> removerEmissor(numeroConta, emissor));
        emissor.onTimeout(() -> removerEmissor(numeroConta, emissor));
        emissor.onError(e -> removerEmissor(numeroConta, emissor));

        log.info("Novo emitter SSE criado para conta: {}", numeroConta);

        // Enviar mensagem de conexão estabelecida
        try {
            emissor.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"Conexão SSE estabelecida com sucesso\"}"));
        } catch (IOException e) {
            log.warn("Erro ao enviar mensagem de conexão para conta {}: {}", numeroConta, e.getMessage());
        }

        return emissor;
    }

    public void enviarNotificacao(String numeroConta, NotificacaoDTO notificacao) {
        // Adicionar ao histórico
        historicoPorConta.computeIfAbsent(numeroConta, k -> new ArrayList<>()).add(notificacao);

        List<SseEmitter> emissores = emissoresPorConta.get(numeroConta);
        if (emissores == null || emissores.isEmpty()) {
            log.debug("Nenhum emitter ativo para conta: {}", numeroConta);
            return;
        }

        List<SseEmitter> parRemover = new ArrayList<>();
        for (SseEmitter emissor : emissores) {
            try {
                String json = objectMapper.writeValueAsString(notificacao);
                emissor.send(SseEmitter.event()
                        .name("notification") // Nome do evento
                        .data(json));
                log.debug("Notificação enviada via SSE para conta: {}", numeroConta);
            } catch (IOException ex) {
                log.warn("Erro ao enviar SSE para conta {}: {}", numeroConta, ex.getMessage());
                parRemover.add(emissor);
            }
        }
        emissores.removeAll(parRemover);
    }

    public List<NotificacaoDTO> obterHistorico(String numeroConta) {
        return historicoPorConta.getOrDefault(numeroConta, new ArrayList<>());
    }

    private void removerEmissor(String numeroConta, SseEmitter emissor) {
        List<SseEmitter> emissores = emissoresPorConta.get(numeroConta);
        if (emissores != null) {
            emissores.remove(emissor);
            log.info("Emitter SSE removido para conta: {}", numeroConta);
        }
    }

    // Método para limpar histórico (opcional, para evitar acúmulo de memória)
    public void limparHistorico(String numeroConta) {
        historicoPorConta.remove(numeroConta);
    }
}