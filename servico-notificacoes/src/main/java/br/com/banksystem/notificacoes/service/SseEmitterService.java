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
        return emissor;
    }

    public void enviarNotificacao(String numeroConta, NotificacaoDTO notificacao) {
        List<SseEmitter> emissores = emissoresPorConta.get(numeroConta);
        if (emissores == null || emissores.isEmpty()) return;

        List<SseEmitter> parRemover = new ArrayList<>();
        for (SseEmitter emissor : emissores) {
            try {
                String json = objectMapper.writeValueAsString(notificacao);
                emissor.send(SseEmitter.event().data(json));
            } catch (IOException ex) {
                log.warn("Erro ao enviar SSE para conta {}: {}", numeroConta, ex.getMessage());
                parRemover.add(emissor);
            }
        }
        emissores.removeAll(parRemover);
    }

    private void removerEmissor(String numeroConta, SseEmitter emissor) {
        List<SseEmitter> emissores = emissoresPorConta.get(numeroConta);
        if (emissores != null) emissores.remove(emissor);
    }
}
