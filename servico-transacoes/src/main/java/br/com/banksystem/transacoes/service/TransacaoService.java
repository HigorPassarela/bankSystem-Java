package br.com.banksystem.transacoes.service;

import br.com.banksystem.transacoes.client.ContasClient;
import br.com.banksystem.transacoes.dto.*;
import br.com.banksystem.transacoes.exception.SaldoInsuficienteException;
import br.com.banksystem.transacoes.exception.TransferenciaInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Serviço de processamento de transações bancárias.
 *
 * Operações disponíveis:
 *  - Depósito     → credita no saldo (INCR Redis)
 *  - Débito       → debita do saldo  (DECR Redis, valida saldo)
 *  - Crédito      → usa limite       (DECR Redis limite)
 *  - Transferência → débito origem + crédito destino (atômico, exige PIN)
 *
 * Todas publicam evento no Kafka → extratos + notificações + fraudes.
 */
@Service
public class TransacaoService {

    private static final Logger log = LoggerFactory.getLogger(TransacaoService.class);
    private static final BigDecimal CENTAVOS = new BigDecimal("100");

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, TransacaoEventoDTO> kafkaTemplate;
    private final ContasClient contasClient;

    public TransacaoService(RedisTemplate<String, String> redisTemplate,
                            KafkaTemplate<String, TransacaoEventoDTO> kafkaTemplate,
                            ContasClient contasClient) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.contasClient = contasClient;
    }

    // ── DEPÓSITO ──────────────────────────────────────────────────────────────

    /**
     * Realiza depósito em conta — incrementa o saldo disponível no Redis.
     * Não exige PIN de transferência, apenas autenticação JWT.
     * Publica evento DEPOSITO no tópico transacoes-aprovadas.
     */
    public TransacaoRespostaDTO processarDeposito(String numeroConta, DepositoDTO dto) {
        log.info("Processando depósito de R$ {} na conta {}", dto.valor(), numeroConta);

        long valorCentavos = toCentavos(dto.valor());
        String chaveSaldo = "saldo:" + numeroConta;

        redisTemplate.opsForValue().increment(chaveSaldo, valorCentavos);

        String saldoStr = redisTemplate.opsForValue().get(chaveSaldo);
        long novoSaldo = saldoStr != null ? Long.parseLong(saldoStr) : valorCentavos;

        String idTransacao = UUID.randomUUID().toString();
        String descricao = dto.descricao() != null ? dto.descricao() : "Depósito em conta";

        publicarAprovada(
                idTransacao,
                numeroConta,
                dto.valor(),
                "DEPOSITO",
                descricao,
                fromCentavos(novoSaldo)
        );

        log.info("Depósito aprovado. ID: {} | Novo saldo: R$ {}", idTransacao, fromCentavos(novoSaldo));

        return new TransacaoRespostaDTO(
                idTransacao,
                numeroConta,
                dto.valor(),
                "DEPOSITO",
                "APROVADA",
                fromCentavos(novoSaldo),
                LocalDateTime.now()
        );
    }

    // ── DÉBITO ────────────────────────────────────────────────────────────────

    /**
     * Débito no saldo disponível — verifica saldo antes de descontar.
     */
    public TransacaoRespostaDTO processarDebito(String numeroConta, DebitoDTO dto) {
        log.info("Processando débito de R$ {} na conta {}", dto.valor(), numeroConta);

        long valorCentavos = toCentavos(dto.valor());
        String chaveSaldo = "saldo:" + numeroConta;
        long saldoAtual = getSaldo(chaveSaldo);

        if (saldoAtual < valorCentavos) {
            publicarReprovada(numeroConta, dto.valor(), "DEBITO", dto.descricao());
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Disponível: R$ " + fromCentavos(saldoAtual));
        }

        redisTemplate.opsForValue().decrement(chaveSaldo, valorCentavos);
        long novoSaldo = saldoAtual - valorCentavos;
        String idTransacao = UUID.randomUUID().toString();

        publicarAprovada(
                idTransacao,
                numeroConta,
                dto.valor(),
                "DEBITO",
                dto.descricao(),
                fromCentavos(novoSaldo)
        );

        return new TransacaoRespostaDTO(
                idTransacao,
                numeroConta,
                dto.valor(),
                "DEBITO",
                "APROVADA",
                fromCentavos(novoSaldo),
                LocalDateTime.now()
        );
    }

    // ── CRÉDITO ───────────────────────────────────────────────────────────────

    /**
     * Crédito usando limite disponível (modalidade "pagar depois").
     */
    public TransacaoRespostaDTO processarCredito(String numeroConta, CreditoDTO dto) {
        log.info("Processando crédito de R$ {} na conta {}", dto.valor(), numeroConta);

        long valorCentavos = toCentavos(dto.valor());
        String chaveLimite = "limite:" + numeroConta;
        long limiteAtual = getSaldo(chaveLimite);

        if (limiteAtual < valorCentavos) {
            publicarReprovada(numeroConta, dto.valor(), "CREDITO", dto.descricao());
            throw new SaldoInsuficienteException(
                    "Limite insuficiente. Disponível: R$ " + fromCentavos(limiteAtual));
        }

        redisTemplate.opsForValue().decrement(chaveLimite, valorCentavos);
        long novoLimite = limiteAtual - valorCentavos;
        String idTransacao = UUID.randomUUID().toString();

        publicarAprovada(
                idTransacao,
                numeroConta,
                dto.valor(),
                "CREDITO",
                dto.descricao(),
                fromCentavos(novoLimite)
        );

        return new TransacaoRespostaDTO(
                idTransacao,
                numeroConta,
                dto.valor(),
                "CREDITO",
                "APROVADA",
                fromCentavos(novoLimite),
                LocalDateTime.now()
        );
    }

    // ── TRANSFERÊNCIA ─────────────────────────────────────────────────────────

    /**
     * Transferência entre contas.
     * 1. Valida PIN de 4 dígitos junto ao servico-contas
     * 2. Verifica saldo da origem
     * 3. Debita origem + credita destino (atômico via Redis)
     * 4. Publica dois eventos Kafka (SAIDA e ENTRADA)
     */
    public TransferenciaRespostaDTO processarTransferencia(String contaOrigem,
                                                           TransferenciaDTO dto,
                                                           String jwtToken) {
        log.info("Transferência de {} → {} | R$ {}", contaOrigem, dto.contaDestino(), dto.valor());

        if (contaOrigem.equals(dto.contaDestino())) {
            throw new TransferenciaInvalidaException("Não é possível transferir para a própria conta");
        }

        boolean pinValido = contasClient.validarSenhaTransferencia(
                contaOrigem, dto.senhaTransferencia(), jwtToken);

        if (!pinValido) {
            throw new TransferenciaInvalidaException("Senha de transferência (PIN) inválida");
        }

        long valorCentavos = toCentavos(dto.valor());
        String chaveSaldoOrigem = "saldo:" + contaOrigem;
        String chaveSaldoDestino = "saldo:" + dto.contaDestino();

        long saldoOrigem = getSaldo(chaveSaldoOrigem);
        long saldoDestino = getSaldo(chaveSaldoDestino);

        if (saldoOrigem < valorCentavos) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente para transferência. Disponível: R$ " + fromCentavos(saldoOrigem));
        }

        redisTemplate.opsForValue().decrement(chaveSaldoOrigem, valorCentavos);
        redisTemplate.opsForValue().increment(chaveSaldoDestino, valorCentavos);

        long novoSaldoOrigem = saldoOrigem - valorCentavos;
        long novoSaldoDestino = saldoDestino + valorCentavos;

        String idTxOrigem = UUID.randomUUID().toString();
        String idTxDestino = UUID.randomUUID().toString();
        String desc = dto.descricao() != null ? " — " + dto.descricao() : "";

        publicarAprovada(
                idTxOrigem,
                contaOrigem,
                dto.valor(),
                "TRANSFERENCIA_SAIDA",
                "Transferência para conta " + dto.contaDestino() + desc,
                fromCentavos(novoSaldoOrigem),
                contaOrigem,
                dto.contaDestino()
        );

        publicarAprovada(
                idTxDestino,
                dto.contaDestino(),
                dto.valor(),
                "TRANSFERENCIA_ENTRADA",
                "Transferência recebida da conta " + contaOrigem + desc,
                fromCentavos(novoSaldoDestino),
                contaOrigem,
                dto.contaDestino()
        );

        log.info("Transferência aprovada. ID: {} | Novo saldo origem: R$ {}",
                idTxOrigem, fromCentavos(novoSaldoOrigem));

        return new TransferenciaRespostaDTO(
                idTxOrigem,
                contaOrigem,
                dto.contaDestino(),
                dto.valor(),
                "APROVADA",
                fromCentavos(novoSaldoOrigem),
                LocalDateTime.now()
        );
    }

    // ── CONSULTA ──────────────────────────────────────────────────────────────

    public SaldoDTO consultarSaldo(String numeroConta) {
        long saldo = getSaldo("saldo:" + numeroConta);
        long limite = getSaldo("limite:" + numeroConta);
        return new SaldoDTO(numeroConta, fromCentavos(saldo), fromCentavos(limite));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void publicarAprovada(String id, String numeroConta,
                                  BigDecimal valor, String tipo, String descricao) {
        publicarAprovada(id, numeroConta, valor, tipo, descricao, null, numeroConta, null);
    }

    private void publicarAprovada(String id, String numeroConta,
                                  BigDecimal valor, String tipo, String descricao,
                                  BigDecimal saldoApos) {
        publicarAprovada(id, numeroConta, valor, tipo, descricao, saldoApos, numeroConta, null);
    }

    private void publicarAprovada(String id, String numeroConta,
                                  BigDecimal valor, String tipo, String descricao,
                                  BigDecimal saldoApos,
                                  String contaOrigem,
                                  String contaDestino) {
        kafkaTemplate.send("transacoes-aprovadas", numeroConta,
                new TransacaoEventoDTO(
                        id,
                        numeroConta,
                        valor,
                        tipo,
                        "APROVADA",
                        descricao,
                        LocalDateTime.now(),
                        saldoApos,
                        contaOrigem,
                        contaDestino
                ));
    }

    private void publicarReprovada(String numeroConta, BigDecimal valor, String tipo, String descricao) {
        kafkaTemplate.send("transacoes-reprovadas", numeroConta,
                new TransacaoEventoDTO(
                        UUID.randomUUID().toString(),
                        numeroConta,
                        valor,
                        tipo,
                        "REPROVADA",
                        descricao,
                        LocalDateTime.now(),
                        null,
                        numeroConta,
                        null
                ));
    }

    private long toCentavos(BigDecimal valor) {
        return valor.multiply(CENTAVOS).longValue();
    }

    private BigDecimal fromCentavos(long centavos) {
        return BigDecimal.valueOf(centavos).divide(CENTAVOS, 2, RoundingMode.HALF_UP);
    }

    private long getSaldo(String chave) {
        String v = redisTemplate.opsForValue().get(chave);
        return v != null ? Long.parseLong(v) : 0L;
    }
}