package br.com.banksystem.transacoes.service;

import br.com.banksystem.transacoes.client.ContasClient;
import br.com.banksystem.transacoes.dto.*;
import br.com.banksystem.transacoes.exception.SaldoInsuficienteException;
import br.com.banksystem.transacoes.exception.TransferenciaInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private KafkaTemplate<String, TransacaoEventoDTO> kafkaTemplate;

    @Mock
    private ContasClient contasClient;

    private TransacaoService transacaoService;

    @BeforeEach
    void setUp() {
        transacaoService = new TransacaoService(redisTemplate, kafkaTemplate, contasClient);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - PROCESSAR DEPÓSITO
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void processarDeposito_DeveRealizarDepositoComSucesso() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("100.50");
        DepositoDTO dto = new DepositoDTO(valor, "Depósito teste");

        when(valueOperations.increment(eq("saldo:" + numeroConta), eq(10050L)))
                .thenReturn(10050L);
        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("10050");

        // Act
        TransacaoRespostaDTO resultado = transacaoService.processarDeposito(numeroConta, dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(numeroConta, resultado.numeroConta());
        assertEquals(valor, resultado.valor());
        assertEquals("DEPOSITO", resultado.tipo());
        assertEquals("APROVADA", resultado.status());
        assertEquals(new BigDecimal("100.50"), resultado.saldoAtualizado());
        assertNotNull(resultado.idTransacao());
        assertNotNull(resultado.dataHora());

        verify(valueOperations).increment("saldo:" + numeroConta, 10050L);
        verify(kafkaTemplate).send(eq("transacoes-aprovadas"), eq(numeroConta), any(TransacaoEventoDTO.class));
    }

    @Test
    void processarDeposito_DeveUsarDescricaoPadrao_QuandoDescricaoForNula() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("50.00");
        DepositoDTO dto = new DepositoDTO(valor, null);

        when(valueOperations.increment(eq("saldo:" + numeroConta), eq(5000L)))
                .thenReturn(5000L);
        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("5000");

        // Act
        TransacaoRespostaDTO resultado = transacaoService.processarDeposito(numeroConta, dto);

        // Assert
        assertNotNull(resultado);
        assertEquals("DEPOSITO", resultado.tipo());
        assertEquals("APROVADA", resultado.status());

        verify(kafkaTemplate).send(eq("transacoes-aprovadas"), eq(numeroConta),
                argThat(evento -> evento.descricao().equals("Depósito em conta")));
    }

    @Test
    void processarDeposito_DeveCalcularSaldoCorretamente_QuandoSaldoAnteriorExiste() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("25.75");
        DepositoDTO dto = new DepositoDTO(valor, "Segundo depósito");

        when(valueOperations.increment(eq("saldo:" + numeroConta), eq(2575L)))
                .thenReturn(12575L);
        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("12575");

        // Act
        TransacaoRespostaDTO resultado = transacaoService.processarDeposito(numeroConta, dto);

        // Assert
        assertEquals(new BigDecimal("125.75"), resultado.saldoAtualizado());
        verify(valueOperations).increment("saldo:" + numeroConta, 2575L);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - PROCESSAR DÉBITO
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void processarDebito_DeveRealizarDebitoComSucesso_QuandoSaldoSuficiente() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("50.25");
        DebitoDTO dto = new DebitoDTO(valor, "Débito teste");

        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("10000");
        when(valueOperations.decrement(eq("saldo:" + numeroConta), eq(5025L)))
                .thenReturn(4975L);

        // Act
        TransacaoRespostaDTO resultado = transacaoService.processarDebito(numeroConta, dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(numeroConta, resultado.numeroConta());
        assertEquals(valor, resultado.valor());
        assertEquals("DEBITO", resultado.tipo());
        assertEquals("APROVADA", resultado.status());
        assertEquals(new BigDecimal("49.75"), resultado.saldoAtualizado());
        assertNotNull(resultado.idTransacao());

        verify(valueOperations).get("saldo:" + numeroConta);
        verify(valueOperations).decrement("saldo:" + numeroConta, 5025L);
        verify(kafkaTemplate).send(eq("transacoes-aprovadas"), eq(numeroConta), any(TransacaoEventoDTO.class));
    }

    @Test
    void processarDebito_DeveLancarSaldoInsuficienteException_QuandoSaldoInsuficiente() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("150.00");
        DebitoDTO dto = new DebitoDTO(valor, "Débito maior que saldo");

        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("10000");

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> transacaoService.processarDebito(numeroConta, dto)
        );

        assertEquals("Saldo insuficiente. Disponível: R$ 100.00", exception.getMessage());

        verify(valueOperations, never()).decrement(anyString(), anyLong());
        verify(kafkaTemplate).send(eq("transacoes-reprovadas"), eq(numeroConta),
                argThat(evento -> evento.status().equals("REPROVADA") && evento.tipo().equals("DEBITO")));
    }

    @Test
    void processarDebito_DeveProcessarCorretamente_QuandoSaldoExatoIgualValor() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("100.00");
        DebitoDTO dto = new DebitoDTO(valor, "Débito total do saldo");

        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("10000");
        when(valueOperations.decrement(eq("saldo:" + numeroConta), eq(10000L)))
                .thenReturn(0L);

        // Act
        TransacaoRespostaDTO resultado = transacaoService.processarDebito(numeroConta, dto);

        // Assert
        assertEquals("APROVADA", resultado.status());
        assertEquals(new BigDecimal("0.00"), resultado.saldoAtualizado());

        verify(valueOperations).decrement("saldo:" + numeroConta, 10000L);
        verify(kafkaTemplate).send(eq("transacoes-aprovadas"), eq(numeroConta), any(TransacaoEventoDTO.class));
    }

    @Test
    void processarDebito_DeveProcessarCorretamente_QuandoSaldoRedisNulo() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("10.00");
        DebitoDTO dto = new DebitoDTO(valor, "Débito com saldo nulo");

        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn(null);

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> transacaoService.processarDebito(numeroConta, dto)
        );

        assertEquals("Saldo insuficiente. Disponível: R$ 0.00", exception.getMessage());

        verify(valueOperations, never()).decrement(anyString(), anyLong());
        verify(kafkaTemplate).send(eq("transacoes-reprovadas"), eq(numeroConta), any(TransacaoEventoDTO.class));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - PROCESSAR CRÉDITO
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void processarCredito_DeveRealizarCreditoComSucesso_QuandoLimiteSuficiente() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("200.00");
        CreditoDTO dto = new CreditoDTO(valor, "Crédito teste");

        when(valueOperations.get("limite:" + numeroConta))
                .thenReturn("50000"); // R$ 500,00 de limite
        when(valueOperations.decrement(eq("limite:" + numeroConta), eq(20000L)))
                .thenReturn(30000L);

        // Act
        TransacaoRespostaDTO resultado = transacaoService.processarCredito(numeroConta, dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(numeroConta, resultado.numeroConta());
        assertEquals(valor, resultado.valor());
        assertEquals("CREDITO", resultado.tipo());
        assertEquals("APROVADA", resultado.status());
        assertEquals(new BigDecimal("300.00"), resultado.saldoAtualizado()); // Limite restante
        assertNotNull(resultado.idTransacao());

        verify(valueOperations).get("limite:" + numeroConta);
        verify(valueOperations).decrement("limite:" + numeroConta, 20000L);
        verify(kafkaTemplate).send(eq("transacoes-aprovadas"), eq(numeroConta), any(TransacaoEventoDTO.class));
    }

    @Test
    void processarCredito_DeveLancarSaldoInsuficienteException_QuandoLimiteInsuficiente() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("600.00");
        CreditoDTO dto = new CreditoDTO(valor, "Crédito acima do limite");

        when(valueOperations.get("limite:" + numeroConta))
                .thenReturn("50000"); // R$ 500,00 de limite

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> transacaoService.processarCredito(numeroConta, dto)
        );

        assertEquals("Limite insuficiente. Disponível: R$ 500.00", exception.getMessage());

        verify(valueOperations, never()).decrement(anyString(), anyLong());
        verify(kafkaTemplate).send(eq("transacoes-reprovadas"), eq(numeroConta),
                argThat(evento -> evento.status().equals("REPROVADA") && evento.tipo().equals("CREDITO")));
    }

    @Test
    void processarCredito_DeveProcessarCorretamente_QuandoLimiteRedisNulo() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        BigDecimal valor = new BigDecimal("100.00");
        CreditoDTO dto = new CreditoDTO(valor, "Crédito sem limite");

        when(valueOperations.get("limite:" + numeroConta))
                .thenReturn(null);

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> transacaoService.processarCredito(numeroConta, dto)
        );

        assertEquals("Limite insuficiente. Disponível: R$ 0.00", exception.getMessage());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - PROCESSAR TRANSFERÊNCIA
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void processarTransferencia_DeveRealizarTransferenciaComSucesso() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String contaOrigem = "12345678";
        String contaDestino = "87654321";
        BigDecimal valor = new BigDecimal("150.00");
        TransferenciaDTO dto = new TransferenciaDTO(contaDestino, valor, "1234", "Transferência teste");
        String jwtToken = "token-jwt";

        when(contasClient.validarSenhaTransferencia(contaOrigem, "1234", jwtToken))
                .thenReturn(true);
        when(valueOperations.get("saldo:" + contaOrigem))
                .thenReturn("20000"); // R$ 200,00
        when(valueOperations.get("saldo:" + contaDestino))
                .thenReturn("10000"); // R$ 100,00
        when(valueOperations.decrement(eq("saldo:" + contaOrigem), eq(15000L)))
                .thenReturn(5000L);
        when(valueOperations.increment(eq("saldo:" + contaDestino), eq(15000L)))
                .thenReturn(25000L);

        // Act
        TransferenciaRespostaDTO resultado = transacaoService.processarTransferencia(contaOrigem, dto, jwtToken);

        // Assert
        assertNotNull(resultado);
        assertEquals(contaOrigem, resultado.contaOrigem());
        assertEquals(contaDestino, resultado.contaDestino());
        assertEquals(valor, resultado.valor());
        assertEquals("APROVADA", resultado.status());
        assertEquals(new BigDecimal("50.00"), resultado.saldoOrigemAtualizado());
        assertNotNull(resultado.idTransacao());

        verify(contasClient).validarSenhaTransferencia(contaOrigem, "1234", jwtToken);
        verify(valueOperations).decrement("saldo:" + contaOrigem, 15000L);
        verify(valueOperations).increment("saldo:" + contaDestino, 15000L);
        verify(kafkaTemplate, times(2)).send(eq("transacoes-aprovadas"), anyString(), any(TransacaoEventoDTO.class));
    }

    @Test
    void processarTransferencia_DeveLancarException_QuandoContaOrigemIgualDestino() {
        // Arrange
        String numeroConta = "12345678";
        TransferenciaDTO dto = new TransferenciaDTO(numeroConta, new BigDecimal("100.00"), "1234", "Transferência");
        String jwtToken = "token-jwt";

        // Act & Assert
        TransferenciaInvalidaException exception = assertThrows(
                TransferenciaInvalidaException.class,
                () -> transacaoService.processarTransferencia(numeroConta, dto, jwtToken)
        );

        assertEquals("Não é possível transferir para a própria conta", exception.getMessage());
        verifyNoInteractions(contasClient);
    }

    @Test
    void processarTransferencia_DeveLancarException_QuandoPinInvalido() {
        // Arrange
        String contaOrigem = "12345678";
        String contaDestino = "87654321";
        TransferenciaDTO dto = new TransferenciaDTO(contaDestino, new BigDecimal("100.00"), "0000", "Transferência");
        String jwtToken = "token-jwt";

        when(contasClient.validarSenhaTransferencia(contaOrigem, "0000", jwtToken))
                .thenReturn(false);

        // Act & Assert
        TransferenciaInvalidaException exception = assertThrows(
                TransferenciaInvalidaException.class,
                () -> transacaoService.processarTransferencia(contaOrigem, dto, jwtToken)
        );

        assertEquals("Senha de transferência (PIN) inválida", exception.getMessage());
        verify(contasClient).validarSenhaTransferencia(contaOrigem, "0000", jwtToken);
    }

    @Test
    void processarTransferencia_DeveLancarSaldoInsuficienteException_QuandoSaldoInsuficiente() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String contaOrigem = "12345678";
        String contaDestino = "87654321";
        TransferenciaDTO dto = new TransferenciaDTO(contaDestino, new BigDecimal("300.00"), "1234", "Transferência");
        String jwtToken = "token-jwt";

        when(contasClient.validarSenhaTransferencia(contaOrigem, "1234", jwtToken))
                .thenReturn(true);
        when(valueOperations.get("saldo:" + contaOrigem))
                .thenReturn("20000"); // R$ 200,00

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(
                SaldoInsuficienteException.class,
                () -> transacaoService.processarTransferencia(contaOrigem, dto, jwtToken)
        );

        assertEquals("Saldo insuficiente para transferência. Disponível: R$ 200.00", exception.getMessage());
        verify(valueOperations, never()).decrement(anyString(), anyLong());
        verify(valueOperations, never()).increment(anyString(), anyLong());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TESTES - CONSULTAR SALDO
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void consultarSaldo_DeveRetornarSaldoELimiteCorretamente() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn("15000"); // R$ 150,00
        when(valueOperations.get("limite:" + numeroConta))
                .thenReturn("50000"); // R$ 500,00

        // Act
        SaldoDTO resultado = transacaoService.consultarSaldo(numeroConta);

        // Assert
        assertNotNull(resultado);
        assertEquals(numeroConta, resultado.numeroConta());
        assertEquals(new BigDecimal("150.00"), resultado.saldoDisponivel());
        assertEquals(new BigDecimal("500.00"), resultado.limiteDisponivel());

        verify(valueOperations).get("saldo:" + numeroConta);
        verify(valueOperations).get("limite:" + numeroConta);
    }

    @Test
    void consultarSaldo_DeveRetornarZero_QuandoSaldoELimiteNulos() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn(null);
        when(valueOperations.get("limite:" + numeroConta))
                .thenReturn(null);

        // Act
        SaldoDTO resultado = transacaoService.consultarSaldo(numeroConta);

        // Assert
        assertNotNull(resultado);
        assertEquals(numeroConta, resultado.numeroConta());
        assertEquals(new BigDecimal("0.00"), resultado.saldoDisponivel());
        assertEquals(new BigDecimal("0.00"), resultado.limiteDisponivel());
    }

    @Test
    void consultarSaldo_DeveRetornarSaldoZeroELimiteValido_QuandoApenasSaldoNulo() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String numeroConta = "12345678";
        when(valueOperations.get("saldo:" + numeroConta))
                .thenReturn(null);
        when(valueOperations.get("limite:" + numeroConta))
                .thenReturn("25000"); // R$ 250,00

        // Act
        SaldoDTO resultado = transacaoService.consultarSaldo(numeroConta);

        // Assert
        assertEquals(new BigDecimal("0.00"), resultado.saldoDisponivel());
        assertEquals(new BigDecimal("250.00"), resultado.limiteDisponivel());
    }
}