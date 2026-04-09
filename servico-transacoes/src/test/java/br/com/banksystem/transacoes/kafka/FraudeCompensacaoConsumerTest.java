package br.com.banksystem.transacoes.kafka;

import br.com.banksystem.transacoes.dto.EstornoFraudeDTO;
import br.com.banksystem.transacoes.dto.FraudeDetectadaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudeCompensacaoConsumerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private KafkaTemplate<String, EstornoFraudeDTO> estornoKafkaTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private FraudeCompensacaoConsumer fraudeCompensacaoConsumer;

    private final String numeroConta = "12345678";
    private final String idTransacao = "txn-123";
    private final BigDecimal valor = new BigDecimal("100.50");

    @BeforeEach
    void setUp() {
        fraudeCompensacaoConsumer = new FraudeCompensacaoConsumer(redisTemplate, estornoKafkaTemplate);
    }

    @Test
    void consumirFraudeDetectada_DeveCompensarDeposito_QuandoTipoDeposito() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "DEPOSITO", 1, "descricao", LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).decrement("saldo:" + numeroConta, 10050L);

        ArgumentCaptor<EstornoFraudeDTO> estornoCaptor = ArgumentCaptor.forClass(EstornoFraudeDTO.class);
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), estornoCaptor.capture());

        EstornoFraudeDTO estorno = estornoCaptor.getValue();
        assertThat(estorno).isNotNull();
    }

    @Test
    void consumirFraudeDetectada_DeveCompensarDebito_QuandoTipoDebito() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "DEBITO", 1, "descricao", LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).increment("saldo:" + numeroConta, 10050L);
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveCompensarCredito_QuandoTipoCredito() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "CREDITO", 1, "descricao", LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).increment("limite:" + numeroConta, 10050L);
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveCompensarTransferencia_QuandoTipoTransferenciaSaida() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String contaOrigem = "12345678";
        String contaDestino = "87654321";
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "TRANSFERENCIA_SAIDA", 1, "descricao",
                LocalDateTime.now(), contaOrigem, contaDestino
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).increment("saldo:" + contaOrigem, 10050L);
        verify(valueOperations).decrement("saldo:" + contaDestino, 10050L);
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveIgnorarTransferenciaEntrada() {
        // Given - Não precisamos mockar Redis/Kafka pois não serão usados
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "TRANSFERENCIA_ENTRADA", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveIgnorarTipoDesconhecido() {
        // Given
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "TIPO_DESCONHECIDO", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveIgnorarEventoInvalido_QuandoValorNulo() {
        // Given
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, null, "DEPOSITO", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveIgnorarEventoInvalido_QuandoNumeroContaNulo() {
        // Given
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, null, valor, "DEPOSITO", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveIgnorarEventoInvalido_QuandoTipoNulo() {
        // Given
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, null, 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_NaoDeveCompensarTransferencia_QuandoContaOrigemNula() {
        // Given
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "TRANSFERENCIA_SAIDA", 1, "descricao",
                LocalDateTime.now(), null, "87654321"
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_NaoDeveCompensarTransferencia_QuandoContaDestinoNula() {
        // Given
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "TRANSFERENCIA_SAIDA", 1, "descricao",
                LocalDateTime.now(), "12345678", null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(redisTemplate, never()).opsForValue();
        verify(estornoKafkaTemplate, never()).send(anyString(), anyString(), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveConverterValorParaCentavos_Corretamente() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        BigDecimal valorTeste = new BigDecimal("123.45");
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valorTeste, "DEPOSITO", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).decrement("saldo:" + numeroConta, 12345L); // 123.45 * 100
    }

    @Test
    void consumirFraudeDetectada_DeveConverterValorZeroParaCentavos() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        BigDecimal valorZero = BigDecimal.ZERO;
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valorZero, "DEPOSITO", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).decrement("saldo:" + numeroConta, 0L);
    }

    @Test
    void consumirFraudeDetectada_DeveUsarChaveRedisCorreta_ParaCadaTipoTransacao() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Test DEPOSITO
        FraudeDetectadaDTO eventoDeposito = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "DEPOSITO", 1, "desc", LocalDateTime.now(), null, null
        );
        fraudeCompensacaoConsumer.consumirFraudeDetectada(eventoDeposito);
        verify(valueOperations).decrement("saldo:" + numeroConta, 10050L);

        // Test DEBITO
        FraudeDetectadaDTO eventoDebito = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "DEBITO", 1, "desc", LocalDateTime.now(), null, null
        );
        fraudeCompensacaoConsumer.consumirFraudeDetectada(eventoDebito);
        verify(valueOperations).increment("saldo:" + numeroConta, 10050L);

        // Test CREDITO
        FraudeDetectadaDTO eventoCredito = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "CREDITO", 1, "desc", LocalDateTime.now(), null, null
        );
        fraudeCompensacaoConsumer.consumirFraudeDetectada(eventoCredito);
        verify(valueOperations).increment("limite:" + numeroConta, 10050L);
    }

    @Test
    void publicarEstorno_DeveEnviarEventoComDadosCorretos() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String contaOrigem = "12345678";
        String contaDestino = "87654321";
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valor, "TRANSFERENCIA_SAIDA", 1, "descricao",
                LocalDateTime.now(), contaOrigem, contaDestino
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        ArgumentCaptor<EstornoFraudeDTO> captor = ArgumentCaptor.forClass(EstornoFraudeDTO.class);
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), captor.capture());

        EstornoFraudeDTO estorno = captor.getValue();
        assertThat(estorno).isNotNull();
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveProcessarEventosComValoresDecimais() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        BigDecimal valorDecimal = new BigDecimal("99.99");
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, valorDecimal, "DEBITO", 1, "descricao",
                LocalDateTime.now(), null, null
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).increment("saldo:" + numeroConta, 9999L); // 99.99 * 100
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), any(EstornoFraudeDTO.class));
    }

    @Test
    void consumirFraudeDetectada_DeveProcessarTransferenciaCompleta() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String contaOrigem = "11111111";
        String contaDestino = "22222222";
        FraudeDetectadaDTO evento = new FraudeDetectadaDTO(
                idTransacao, numeroConta, new BigDecimal("50.00"), "TRANSFERENCIA_SAIDA", 1,
                "transferencia suspeita", LocalDateTime.now(), contaOrigem, contaDestino
        );

        // When
        fraudeCompensacaoConsumer.consumirFraudeDetectada(evento);

        // Then
        verify(valueOperations).increment("saldo:" + contaOrigem, 5000L);
        verify(valueOperations).decrement("saldo:" + contaDestino, 5000L);
        verify(estornoKafkaTemplate).send(eq("transacoes-estornadas"), eq(numeroConta), any(EstornoFraudeDTO.class));
    }
}