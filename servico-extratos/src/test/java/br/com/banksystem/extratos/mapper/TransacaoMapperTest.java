package br.com.banksystem.extratos.mapper;

import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.dto.TransacaoEventoDTO;
import br.com.banksystem.extratos.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransacaoMapperTest {

    private TransacaoMapper transacaoMapper;

    @BeforeEach
    void setUp() {
        transacaoMapper = new TransacaoMapper();
    }

    @Test
    void deveConverterEventoDTOParaEntidade() {
        LocalDateTime dataHora = LocalDateTime.of(2024, 1, 15, 10, 30);

        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                "TXN-001",
                "12345",
                new BigDecimal("100.00"),
                "DEPOSITO",
                "APROVADA",
                "Depósito em dinheiro",
                dataHora,
                new BigDecimal("1000.00"),
                "0001",
                "0002"
        );

        Transacao transacao = transacaoMapper.paraEntidade(dto);

        assertNotNull(transacao);
        assertEquals("TXN-001", transacao.getIdTransacao());
        assertEquals("12345", transacao.getNumeroConta());
        assertEquals(new BigDecimal("100.00"), transacao.getValor());
        assertEquals("DEPOSITO", transacao.getTipo());
        assertEquals("APROVADA", transacao.getStatus());
        assertEquals("Depósito em dinheiro", transacao.getDescricao());
        assertEquals(dataHora, transacao.getDataHora());
        assertEquals(new BigDecimal("1000.00"), transacao.getSaldoAposTransacao());
    }

    @Test
    void deveUsarDataAtualQuandoDataHoraForNula() {
        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                "TXN-002",
                "12345",
                new BigDecimal("50.00"),
                "DEBITO",
                "APROVADA",
                "Débito teste",
                null,
                new BigDecimal("950.00"),
                "0001",
                "0002"
        );

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Transacao transacao = transacaoMapper.paraEntidade(dto);

        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);

        assertNotNull(transacao.getDataHora());
        assertTrue(transacao.getDataHora().isAfter(antes));
        assertTrue(transacao.getDataHora().isBefore(depois));
    }

    @Test
    void deveConverterEntidadeParaDTO() {
        LocalDateTime dataHora = LocalDateTime.of(2024, 1, 15, 10, 30);

        Transacao transacao = new Transacao();
        transacao.setIdTransacao("TXN-001");
        transacao.setNumeroConta("12345");
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setTipo("DEPOSITO");
        transacao.setStatus("APROVADA");
        transacao.setDescricao("Depósito em dinheiro");
        transacao.setDataHora(dataHora);
        transacao.setSaldoAposTransacao(new BigDecimal("1000.00"));

        TransacaoDTO dto = transacaoMapper.paraDTO(transacao);

        assertNotNull(dto);
        assertEquals("TXN-001", dto.idTransacao());
        assertEquals("12345", dto.numeroConta());
        assertEquals(new BigDecimal("100.00"), dto.valor());
        assertEquals("DEPOSITO", dto.tipo());
        assertEquals("APROVADA", dto.status());
        assertEquals("Depósito em dinheiro", dto.descricao());
        assertEquals(dataHora, dto.dataHora());
        assertEquals(new BigDecimal("1000.00"), dto.saldoAposTransacao());
    }

    @Test
    void deveConverterEntidadeParaDTOComCamposNulos() {
        Transacao transacao = new Transacao();
        transacao.setIdTransacao(null);
        transacao.setNumeroConta(null);
        transacao.setValor(null);
        transacao.setTipo(null);
        transacao.setStatus(null);
        transacao.setDescricao(null);
        transacao.setDataHora(null);
        transacao.setSaldoAposTransacao(null);

        TransacaoDTO dto = transacaoMapper.paraDTO(transacao);

        assertNotNull(dto);
        assertNull(dto.idTransacao());
        assertNull(dto.numeroConta());
        assertNull(dto.valor());
        assertNull(dto.tipo());
        assertNull(dto.status());
        assertNull(dto.descricao());
        assertNull(dto.dataHora());
        assertNull(dto.saldoAposTransacao());
    }

    @Test
    void deveConverterEventoDTOComCamposNulos() {
        TransacaoEventoDTO dto = new TransacaoEventoDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Transacao transacao = transacaoMapper.paraEntidade(dto);

        assertNotNull(transacao);
        assertNull(transacao.getIdTransacao());
        assertNull(transacao.getNumeroConta());
        assertNull(transacao.getValor());
        assertNull(transacao.getTipo());
        assertNull(transacao.getStatus());
        assertNull(transacao.getDescricao());
        assertNotNull(transacao.getDataHora());
        assertNull(transacao.getSaldoAposTransacao());
    }
}