package br.com.banksystem.extratos.kafka;

import br.com.banksystem.extratos.dto.TransacaoEventoDTO;
import br.com.banksystem.extratos.mapper.TransacaoMapper;
import br.com.banksystem.extratos.model.Transacao;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka — persiste transações no MongoDB.
 *
 * Idempotência: verifica se o idTransacao já existe antes de salvar,
 * evitando duplicatas em caso de reprocessamento de mensagens.
 */
@Component
public class TransacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransacaoConsumer.class);

    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper transacaoMapper;

    public TransacaoConsumer(TransacaoRepository transacaoRepository,
                             TransacaoMapper transacaoMapper) {
        this.transacaoRepository = transacaoRepository;
        this.transacaoMapper = transacaoMapper;
    }

    @KafkaListener(
            topics = "transacoes-aprovadas",
            groupId = "servico-extratos",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirTransacaoAprovada(TransacaoEventoDTO evento) {
        log.info("Recebendo transação aprovada do Kafka: {} | tipo: {} | conta: {}",
                evento.idTransacao(), evento.tipo(), evento.numeroConta());
        persistirSeNaoExistir(evento);
    }

    @KafkaListener(
            topics = "transacoes-reprovadas",
            groupId = "servico-extratos",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirTransacaoReprovada(TransacaoEventoDTO evento) {
        log.info("Recebendo transação reprovada do Kafka: {} | tipo: {} | conta: {}",
                evento.idTransacao(), evento.tipo(), evento.numeroConta());
        persistirSeNaoExistir(evento);
    }

    private void persistirSeNaoExistir(TransacaoEventoDTO evento) {
        if (evento.idTransacao() == null) {
            log.warn("Transação recebida sem ID — ignorando");
            return;
        }

        if (transacaoRepository.existsByIdTransacao(evento.idTransacao())) {
            log.info("Transação {} já persistida — ignorando duplicata", evento.idTransacao());
            return;
        }

        try {
            Transacao transacao = transacaoMapper.paraEntidade(evento);
            transacaoRepository.save(transacao);
            log.info("Transação persistida no MongoDB: {} | tipo: {} | valor: R$ {}",
                    evento.idTransacao(), evento.tipo(), evento.valor());
        } catch (Exception ex) {
            log.error("Erro ao persistir transação {}: {}", evento.idTransacao(), ex.getMessage(), ex);
            throw ex;
        }
    }
}