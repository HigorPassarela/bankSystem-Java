package br.com.banksystem.notificacoes.service;

import br.com.banksystem.notificacoes.dto.NotificacaoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Gerencia os emissores SSE por número de conta para notificações em tempo real.
 */
@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);

    private final Map<String, List<SseEmitter>> emissoresPorConta = new ConcurrentHashMap<>();
    private final Map<String, List<NotificacaoDTO>> historicoPorConta = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitterService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public SseEmitter criarEmissor(String numeroConta) {
        SseEmitter emissor = new SseEmitter(Long.MAX_VALUE);

        emissoresPorConta
                .computeIfAbsent(numeroConta, k -> new CopyOnWriteArrayList<>())
                .add(emissor);

        emissor.onCompletion(() -> removerEmissor(numeroConta, emissor));
        emissor.onTimeout(() -> removerEmissor(numeroConta, emissor));
        emissor.onError(e -> removerEmissor(numeroConta, emissor));

        log.info("Novo emitter SSE criado para conta: {}", numeroConta);

        try {
            emissor.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"Conexão SSE estabelecida com sucesso\"}"));
        } catch (IOException e) {
            log.warn("Erro ao enviar mensagem inicial SSE para conta {}: {}", numeroConta, e.getMessage());
            removerEmissor(numeroConta, emissor);
        }

        return emissor;
    }

    public void enviarNotificacao(String numeroConta, NotificacaoDTO notificacao) {
        historicoPorConta
                .computeIfAbsent(numeroConta, k -> new CopyOnWriteArrayList<>())
                .add(notificacao);

        List<SseEmitter> emissores = emissoresPorConta.get(numeroConta);
        if (emissores == null || emissores.isEmpty()) {
            log.debug("Nenhum emitter ativo para conta: {}", numeroConta);
            return;
        }

        for (SseEmitter emissor : emissores) {
            try {
                String json = objectMapper.writeValueAsString(notificacao);
                emissor.send(SseEmitter.event().data(json));
                log.info("Notificação enviada via SSE para conta: {}", numeroConta);
            } catch (IOException ex) {
                log.warn("Erro ao enviar SSE para conta {}: {}", numeroConta, ex.getMessage());
                removerEmissor(numeroConta, emissor);
            }
        }
    }

    public List<NotificacaoDTO> obterHistorico(String numeroConta) {
        return historicoPorConta.getOrDefault(numeroConta, List.of());
    }

    private void removerEmissor(String numeroConta, SseEmitter emissor) {
        List<SseEmitter> emissores = emissoresPorConta.get(numeroConta);
        if (emissores != null) {
            emissores.remove(emissor);
            log.info("Emitter SSE removido para conta: {}", numeroConta);

            if (emissores.isEmpty()) {
                emissoresPorConta.remove(numeroConta);
            }
        }
    }
}