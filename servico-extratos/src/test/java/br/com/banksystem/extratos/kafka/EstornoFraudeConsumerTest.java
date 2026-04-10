package br.com.banksystem.extratos.kafka;

import br.com.banksystem.extratos.dto.EstornoFraudeDTO;
import br.com.banksystem.extratos.model.Transacao;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstornoFraudeConsumerTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private EstornoFraudeConsumer estornoFraudeConsumer;

    private EstornoFraudeDTO estornoDTO;

    @BeforeEach
    void setUp() {
        estornoDTO = new EstornoFraudeDTO(
                "TXN-001",
                "12345",
                new BigDecimal("100.00"),
                "DEBITO",
                "Transação fraudulenta detectada",
                LocalDateTime.of(2024, 1, 15, 10, 30),
                "0001",
                "0002"
        );
    }

    @Test
    void deveConsumirEstornoFraude() {
        String idEstorno = "ESTORNO-TXN-001";
        when(transacaoRepository.existsByIdTransacao(idEstorno)).thenReturn(false);

        estornoFraudeConsumer.consumirEstorno(estornoDTO);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).existsByIdTransacao(idEstorno);
        verify(transacaoRepository).save(captor.capture());

        Transacao salva = captor.getValue();
        assertEquals("ESTORNO-TXN-001", salva.getIdTransacao());
        assertEquals("12345", salva.getNumeroConta());
        assertEquals(new BigDecimal("100.00"), salva.getValor());
        assertEquals("ESTORNO_FRAUDE", salva.getTipo());
        assertEquals("APROVADA", salva.getStatus());
        assertEquals("Transação fraudulenta detectada", salva.getDescricao());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), salva.getDataHora());
        assertNull(salva.getSaldoAposTransacao());
    }

    @Test
    void deveIgnorarEstornoDuplicado() {
        String idEstorno = "ESTORNO-TXN-001";
        when(transacaoRepository.existsByIdTransacao(idEstorno)).thenReturn(true);

        estornoFraudeConsumer.consumirEstorno(estornoDTO);

        verify(transacaoRepository).existsByIdTransacao(idEstorno);
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveUsarDataAtualQuandoDataHoraForNula() {
        EstornoFraudeDTO estornoSemData = new EstornoFraudeDTO(
                "TXN-002",
                "12345",
                new BigDecimal("50.00"),
                "DEBITO",
                "Estorno sem data",
                null,
                "0001",
                "0002"
        );

        String idEstorno = "ESTORNO-TXN-002";
        when(transacaoRepository.existsByIdTransacao(idEstorno)).thenReturn(false);

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        estornoFraudeConsumer.consumirEstorno(estornoSemData);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());

        Transacao salva = captor.getValue();
        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);

        assertNotNull(salva.getDataHora());
        assertTrue(salva.getDataHora().isAfter(antes));
        assertTrue(salva.getDataHora().isBefore(depois));
    }

    @Test
    void deveGerarIdEstornoCorreto() {
        EstornoFraudeDTO estorno = new EstornoFraudeDTO(
                "TXN-ORIGINAL-123",
                "54321",
                new BigDecimal("200.00"),
                "TRANSFERENCIA",
                "Motivo do estorno",
                LocalDateTime.now(),
                "1111",
                "2222"
        );

        String idEstorno = "ESTORNO-TXN-ORIGINAL-123";
        when(transacaoRepository.existsByIdTransacao(idEstorno)).thenReturn(false);

        estornoFraudeConsumer.consumirEstorno(estorno);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepository).save(captor.capture());

        Transacao salva = captor.getValue();
        assertEquals("ESTORNO-TXN-ORIGINAL-123", salva.getIdTransacao());
        assertEquals("54321", salva.getNumeroConta());
        assertEquals(new BigDecimal("200.00"), salva.getValor());
    }

    @Test
    void deveTratarErroAoSalvarEstorno() {
        String idEstorno = "ESTORNO-TXN-001";
        when(transacaoRepository.existsByIdTransacao(idEstorno)).thenReturn(false);
        when(transacaoRepository.save(any(Transacao.class)))
                .thenThrow(new RuntimeException("Erro de conexão"));

        assertThrows(RuntimeException.class,
                () -> estornoFraudeConsumer.consumirEstorno(estornoDTO));

        verify(transacaoRepository).existsByIdTransacao(idEstorno);
        verify(transacaoRepository).save(any(Transacao.class));
    }
}