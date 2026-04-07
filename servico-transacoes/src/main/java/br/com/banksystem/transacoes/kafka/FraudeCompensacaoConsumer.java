package br.com.banksystem.transacoes.kafka;

import br.com.banksystem.transacoes.dto.EstornoFraudeDTO;
import br.com.banksystem.transacoes.dto.FraudeDetectadaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Consumidor Kafka responsável por compensar transações suspeitas de fraude
 * e publicar evento de estorno.
 */
@Component
public class FraudeCompensacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudeCompensacaoConsumer.class);
    private static final BigDecimal CENTAVOS = new BigDecimal("100");

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, EstornoFraudeDTO> estornoKafkaTemplate;

    public FraudeCompensacaoConsumer(
            RedisTemplate<String, String> redisTemplate,
            @Qualifier("estornoKafkaTemplate") KafkaTemplate<String, EstornoFraudeDTO> estornoKafkaTemplate
    ) {
        this.redisTemplate = redisTemplate;
        this.estornoKafkaTemplate = estornoKafkaTemplate;
    }

    @KafkaListener(
            topics = "transacoes-suspeitas",
            groupId = "servico-transacoes-compensacao",
            containerFactory = "fraudeKafkaListenerContainerFactory"
    )
    public void consumirFraudeDetectada(FraudeDetectadaDTO evento) {
        log.warn("Recebida transação suspeita para compensação: id={} conta={} tipo={} valor={}",
                evento.idTransacao(), evento.numeroConta(), evento.tipo(), evento.valor());

        if (evento.valor() == null || evento.numeroConta() == null || evento.tipo() == null) {
            log.warn("Evento de fraude inválido, ignorando: {}", evento);
            return;
        }

        long valorCentavos = toCentavos(evento.valor());

        switch (evento.tipo()) {
            case "DEPOSITO" -> compensarDeposito(evento, valorCentavos);
            case "DEBITO" -> compensarDebito(evento, valorCentavos);
            case "CREDITO" -> compensarCredito(evento, valorCentavos);
            case "TRANSFERENCIA_SAIDA" -> compensarTransferencia(evento, valorCentavos);
            case "TRANSFERENCIA_ENTRADA" ->
                    log.info("Evento TRANSFERENCIA_ENTRADA ignorado para evitar compensação duplicada. Transação {}",
                            evento.idTransacao());
            default -> log.warn("Tipo de transação desconhecido para compensação: {}", evento.tipo());
        }
    }

    private void compensarDeposito(FraudeDetectadaDTO evento, long valorCentavos) {
        String chaveSaldo = "saldo:" + evento.numeroConta();
        redisTemplate.opsForValue().decrement(chaveSaldo, valorCentavos);

        log.warn("Compensação de DEPÓSITO realizada. Transação {} | Conta {} | Valor {} centavos removidos do saldo",
                evento.idTransacao(), evento.numeroConta(), valorCentavos);

        publicarEstorno(evento, "Depósito estornado por suspeita de fraude");
    }

    private void compensarDebito(FraudeDetectadaDTO evento, long valorCentavos) {
        String chaveSaldo = "saldo:" + evento.numeroConta();
        redisTemplate.opsForValue().increment(chaveSaldo, valorCentavos);

        log.warn("Compensação de DÉBITO realizada. Transação {} | Conta {} | Valor {} centavos devolvidos ao saldo",
                evento.idTransacao(), evento.numeroConta(), valorCentavos);

        publicarEstorno(evento, "Débito estornado por suspeita de fraude");
    }

    private void compensarCredito(FraudeDetectadaDTO evento, long valorCentavos) {
        String chaveLimite = "limite:" + evento.numeroConta();
        redisTemplate.opsForValue().increment(chaveLimite, valorCentavos);

        log.warn("Compensação de CRÉDITO realizada. Transação {} | Conta {} | Valor {} centavos devolvidos ao limite",
                evento.idTransacao(), evento.numeroConta(), valorCentavos);

        publicarEstorno(evento, "Crédito estornado por suspeita de fraude");
    }

    private void compensarTransferencia(FraudeDetectadaDTO evento, long valorCentavos) {
        if (evento.contaOrigem() == null || evento.contaDestino() == null) {
            log.warn("Não foi possível compensar transferência {}: contaOrigem ou contaDestino ausente",
                    evento.idTransacao());
            return;
        }

        String chaveOrigem = "saldo:" + evento.contaOrigem();
        String chaveDestino = "saldo:" + evento.contaDestino();

        redisTemplate.opsForValue().increment(chaveOrigem, valorCentavos);
        redisTemplate.opsForValue().decrement(chaveDestino, valorCentavos);

        log.warn("Compensação de TRANSFERÊNCIA realizada. Transação {} | Origem {} recebeu {} centavos | Destino {} perdeu {} centavos",
                evento.idTransacao(), evento.contaOrigem(), valorCentavos, evento.contaDestino(), valorCentavos);

        publicarEstorno(evento, "Transferência estornada por suspeita de fraude");
    }

    private void publicarEstorno(FraudeDetectadaDTO evento, String motivo) {
        EstornoFraudeDTO estorno = new EstornoFraudeDTO(
                evento.idTransacao(),
                evento.numeroConta(),
                evento.valor(),
                evento.tipo(),
                motivo,
                LocalDateTime.now(),
                evento.contaOrigem(),
                evento.contaDestino()
        );

        estornoKafkaTemplate.send("transacoes-estornadas", evento.numeroConta(), estorno);
        log.warn("Evento de estorno publicado para transação {}", evento.idTransacao());
    }

    private long toCentavos(BigDecimal valor) {
        return valor.multiply(CENTAVOS).longValue();
    }
}