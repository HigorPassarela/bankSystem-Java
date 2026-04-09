package br.com.banksystem.extratos.kafka;

import br.com.banksystem.extratos.dto.TransacaoEventoDTO;
import br.com.banksystem.extratos.mapper.TransacaoMapper;
import br.com.banksystem.extratos.model.Transacao;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoConsumerTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private TransacaoMapper transacaoMapper;

    @InjectMocks
    private TransacaoConsumer transacaoConsumer;

    private TransacaoEventoDTO eventoDTO;
    private Transacao transacao;

    @BeforeEach
    void setUp() {
        // Vou usar uma estrutura com 10 campos baseado no erro anterior
        // Você pode ajustar conforme sua classe real
        eventoDTO = new TransacaoEventoDTO(
                "TXN-001",                    // idTransacao
                "12345",                      // numeroConta
                new BigDecimal("100.00"),     // valor
                "DEPOSITO",                   // tipo
                "APROVADA",                   // status
                "Depósito em dinheiro",       // descricao
                LocalDateTime.now(),          // dataHora
                new BigDecimal("1000.00"),    // saldoAposTransacao
                "67890",                      // contaDestino (assumindo)
                "Observação teste"            // observacoes (assumindo)
        );

        transacao = new Transacao();
        transacao.setIdTransacao("TXN-001");
        transacao.setNumeroConta("12345");
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setTipo("DEPOSITO");
        transacao.setStatus("APROVADA");
        transacao.setDescricao("Depósito em dinheiro");
        transacao.setDataHora(LocalDateTime.now());
        transacao.setSaldoAposTransacao(new BigDecimal("1000.00"));
    }

    @Test
    void deveConsumirTransacaoAprovada() {
        // Given
        when(transacaoRepository.existsByIdTransacao("TXN-001")).thenReturn(false);
        when(transacaoMapper.paraEntidade(eventoDTO)).thenReturn(transacao);

        // When
        transacaoConsumer.consumirTransacaoAprovada(eventoDTO);

        // Then
        verify(transacaoRepository).existsByIdTransacao("TXN-001");
        verify(transacaoMapper).paraEntidade(eventoDTO);
        verify(transacaoRepository).save(transacao);
    }

    @Test
    void deveConsumirTransacaoReprovada() {
        // Given
        TransacaoEventoDTO eventoReprovado = new TransacaoEventoDTO(
                "TXN-002",                    // idTransacao
                "12345",                      // numeroConta
                new BigDecimal("50.00"),      // valor
                "DEBITO",                     // tipo
                "REPROVADA",                  // status
                "Saldo insuficiente",         // descricao
                LocalDateTime.now(),          // dataHora
                new BigDecimal("1000.00"),    // saldoAposTransacao
                null,                         // contaDestino
                "Transação reprovada"         // observacoes
        );

        when(transacaoRepository.existsByIdTransacao("TXN-002")).thenReturn(false);
        when(transacaoMapper.paraEntidade(eventoReprovado)).thenReturn(transacao);

        // When
        transacaoConsumer.consumirTransacaoReprovada(eventoReprovado);

        // Then
        verify(transacaoRepository).existsByIdTransacao("TXN-002");
        verify(transacaoMapper).paraEntidade(eventoReprovado);
        verify(transacaoRepository).save(transacao);
    }

    @Test
    void deveIgnorarTransacaoDuplicada() {
        // Given
        when(transacaoRepository.existsByIdTransacao("TXN-001")).thenReturn(true);

        // When
        transacaoConsumer.consumirTransacaoAprovada(eventoDTO);

        // Then
        verify(transacaoRepository).existsByIdTransacao("TXN-001");
        verify(transacaoMapper, never()).paraEntidade(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveIgnorarTransacaoSemId() {
        // Given
        TransacaoEventoDTO eventoSemId = new TransacaoEventoDTO(
                null,                         // idTransacao = null
                "12345",                      // numeroConta
                new BigDecimal("100.00"),     // valor
                "DEPOSITO",                   // tipo
                "APROVADA",                   // status
                "Depósito",                   // descricao
                LocalDateTime.now(),          // dataHora
                new BigDecimal("1000.00"),    // saldoAposTransacao
                null,                         // contaDestino
                "Teste sem ID"                // observacoes
        );

        // When
        transacaoConsumer.consumirTransacaoAprovada(eventoSemId);

        // Then
        verify(transacaoRepository, never()).existsByIdTransacao(any());
        verify(transacaoMapper, never()).paraEntidade(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoFalharPersistencia() {
        // Given
        when(transacaoRepository.existsByIdTransacao("TXN-001")).thenReturn(false);
        when(transacaoMapper.paraEntidade(eventoDTO)).thenReturn(transacao);
        when(transacaoRepository.save(transacao)).thenThrow(new RuntimeException("Erro no banco"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transacaoConsumer.consumirTransacaoAprovada(eventoDTO));

        assertEquals("Erro no banco", exception.getMessage());
        verify(transacaoRepository).existsByIdTransacao("TXN-001");
        verify(transacaoMapper).paraEntidade(eventoDTO);
        verify(transacaoRepository).save(transacao);
    }

    @Test
    void deveProcessarTransacaoComSucesso() {
        // Given
        when(transacaoRepository.existsByIdTransacao("TXN-001")).thenReturn(false);
        when(transacaoMapper.paraEntidade(eventoDTO)).thenReturn(transacao);

        // When
        transacaoConsumer.consumirTransacaoAprovada(eventoDTO);

        // Then
        verify(transacaoRepository, times(1)).existsByIdTransacao("TXN-001");
        verify(transacaoMapper, times(1)).paraEntidade(eventoDTO);
        verify(transacaoRepository, times(1)).save(transacao);
        verifyNoMoreInteractions(transacaoRepository, transacaoMapper);
    }

    @Test
    void deveVerificarIdempotencia() {
        // Given - Simula que a transação já foi processada
        when(transacaoRepository.existsByIdTransacao("TXN-001")).thenReturn(true);

        // When
        transacaoConsumer.consumirTransacaoAprovada(eventoDTO);

        // Then - Deve verificar existência mas não processar novamente
        verify(transacaoRepository, times(1)).existsByIdTransacao("TXN-001");
        verify(transacaoMapper, never()).paraEntidade(any());
        verify(transacaoRepository, never()).save(any());
    }
}