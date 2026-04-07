package br.com.banksystem.extratos.kafka;

import br.com.banksystem.extratos.dto.EstornoFraudeDTO;
import br.com.banksystem.extratos.model.Transacao;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consumidor Kafka que registra estornos por fraude no extrato.
 */
@Component
public class EstornoFraudeConsumer {

    private static final Logger log = LoggerFactory.getLogger(EstornoFraudeConsumer.class);

    private final TransacaoRepository transacaoRepository;

    public EstornoFraudeConsumer(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    @KafkaListener(
            topics = "transacoes-estornadas",
            groupId = "servico-extratos-estorno",
            containerFactory = "estornoKafkaListenerContainerFactory"
    )
    public void consumirEstorno(EstornoFraudeDTO evento) {
        log.warn("Registrando estorno por fraude no extrato. Conta {} | Transação original {}",
                evento.numeroConta(), evento.idTransacaoOriginal());

        String idTransacaoEstorno = "ESTORNO-" + evento.idTransacaoOriginal();

        if (transacaoRepository.existsByIdTransacao(idTransacaoEstorno)) {
            log.info("Estorno {} já registrado no extrato — ignorando duplicata", idTransacaoEstorno);
            return;
        }

        Transacao transacao = new Transacao();
        transacao.setIdTransacao(idTransacaoEstorno);
        transacao.setNumeroConta(evento.numeroConta());
        transacao.setValor(evento.valor());
        transacao.setTipo("ESTORNO_FRAUDE");
        transacao.setStatus("APROVADA");
        transacao.setDescricao(evento.motivo());
        transacao.setDataHora(evento.dataHora() != null ? evento.dataHora() : LocalDateTime.now());
        transacao.setSaldoAposTransacao(null);

        transacaoRepository.save(transacao);

        log.warn("Estorno por fraude registrado no MongoDB: {}", idTransacaoEstorno);
    }
}