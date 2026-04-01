package br.com.banksystem.transacoes.service;

import br.com.banksystem.transacoes.dto.CreditoDTO;
import br.com.banksystem.transacoes.dto.DebitoDTO;
import br.com.banksystem.transacoes.dto.TransacaoEventoDTO;
import br.com.banksystem.transacoes.dto.TransacaoRespostaDTO;
import br.com.banksystem.transacoes.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
@DisplayName("Testes do TransacaoService")
class TransacaoServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private KafkaTemplate<String, TransacaoEventoDTO> kafkaTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks private TransacaoService transacaoService;

    @BeforeEach
    void configurar() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("Deve processar débito com saldo suficiente")
    void deveProcessarDebitoComSucesso() {
        when(valueOps.get("saldo:00001234")).thenReturn("100000");
        DebitoDTO dto = new DebitoDTO(new BigDecimal("50.00"), "Teste");
        TransacaoRespostaDTO resultado = transacaoService.processarDebito("00001234", dto);
        assertNotNull(resultado);
        assertEquals("APROVADA", resultado.status());
        assertEquals("DEBITO", resultado.tipo());
    }

    @Test
    @DisplayName("Deve lançar exceção com saldo insuficiente")
    void deveLancarExcecaoSaldoInsuficiente() {
        when(valueOps.get("saldo:00001234")).thenReturn("1000");
        DebitoDTO dto = new DebitoDTO(new BigDecimal("500.00"), "Teste");
        assertThrows(SaldoInsuficienteException.class,
                () -> transacaoService.processarDebito("00001234", dto));
    }

    @Test
    @DisplayName("Deve processar crédito com limite suficiente")
    void deveProcessarCreditoComSucesso() {
        when(valueOps.get("limite:00001234")).thenReturn("500000");
        CreditoDTO dto = new CreditoDTO(new BigDecimal("100.00"), "Compra");
        TransacaoRespostaDTO resultado = transacaoService.processarCredito("00001234", dto);
        assertNotNull(resultado);
        assertEquals("APROVADA", resultado.status());
        assertEquals("CREDITO", resultado.tipo());
    }
}
