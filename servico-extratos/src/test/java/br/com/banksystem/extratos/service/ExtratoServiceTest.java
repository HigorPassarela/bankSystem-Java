package br.com.banksystem.extratos.service;

import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.mapper.TransacaoMapper;
import br.com.banksystem.extratos.model.Transacao;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtratoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private TransacaoMapper transacaoMapper;

    @InjectMocks
    private ExtratoService extratoService;

    private Transacao transacao;
    private TransacaoDTO transacaoDTO;

    @BeforeEach
    void setUp() {
        transacao = new Transacao();
        transacao.setIdTransacao("TXN-001");
        transacao.setNumeroConta("12345");
        transacao.setValor(new BigDecimal("100.00"));
        transacao.setTipo("DEPOSITO");
        transacao.setStatus("APROVADA");
        transacao.setDescricao("Depósito em dinheiro");
        transacao.setDataHora(LocalDateTime.of(2024, 1, 15, 10, 30));
        transacao.setSaldoAposTransacao(new BigDecimal("1000.00"));

        transacaoDTO = new TransacaoDTO(
                "TXN-001",
                "12345",
                new BigDecimal("100.00"),
                "DEPOSITO",
                "APROVADA",
                "Depósito em dinheiro",
                LocalDateTime.of(2024, 1, 15, 10, 30),
                new BigDecimal("1000.00")
        );
    }

    @Test
    void deveListarTransacoesPorConta() {
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345"))
                .thenReturn(List.of(transacao));
        when(transacaoMapper.paraDTO(transacao)).thenReturn(transacaoDTO);

        List<TransacaoDTO> resultado = extratoService.listarPorConta("12345");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("TXN-001", resultado.get(0).idTransacao());

        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("12345");
        verify(transacaoMapper).paraDTO(transacao);
    }

    @Test
    void deveListarTransacoesPaginadas() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Transacao> paginaTransacoes = new PageImpl<>(List.of(transacao), pageRequest, 1);

        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345", pageRequest))
                .thenReturn(paginaTransacoes);
        when(transacaoMapper.paraDTO(transacao)).thenReturn(transacaoDTO);

        Page<TransacaoDTO> resultado = extratoService.listarPorContaPaginado("12345", 0, 20);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("TXN-001", resultado.getContent().get(0).idTransacao());

        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("12345", pageRequest);
        verify(transacaoMapper).paraDTO(transacao);
    }

    @Test
    void deveListarTransacoesPorPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(transacaoRepository.findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345", inicio, fim))
                .thenReturn(List.of(transacao));
        when(transacaoMapper.paraDTO(transacao)).thenReturn(transacaoDTO);

        List<TransacaoDTO> resultado = extratoService.listarPorPeriodo("12345", inicio, fim);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("TXN-001", resultado.get(0).idTransacao());

        verify(transacaoRepository).findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345", inicio, fim);
        verify(transacaoMapper).paraDTO(transacao);
    }

    @Test
    void deveListarTransacoesPorTipo() {
        when(transacaoRepository.findByNumeroContaAndTipoOrderByDataHoraDesc("12345", "DEPOSITO"))
                .thenReturn(List.of(transacao));
        when(transacaoMapper.paraDTO(transacao)).thenReturn(transacaoDTO);

        List<TransacaoDTO> resultado = extratoService.listarPorTipo("12345", "DEPOSITO");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("DEPOSITO", resultado.get(0).tipo());

        verify(transacaoRepository).findByNumeroContaAndTipoOrderByDataHoraDesc("12345", "DEPOSITO");
        verify(transacaoMapper).paraDTO(transacao);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverTransacoesPorConta() {
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("99999"))
                .thenReturn(Collections.emptyList());

        List<TransacaoDTO> resultado = extratoService.listarPorConta("99999");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("99999");
        verify(transacaoMapper, never()).paraDTO(any());
    }

    @Test
    void deveGerarPdfCompleto() {
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345"))
                .thenReturn(List.of(transacao));
        when(transacaoMapper.paraDTO(transacao)).thenReturn(transacaoDTO);

        byte[] resultado = extratoService.gerarPdf("12345");

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("12345");
        verify(transacaoMapper).paraDTO(transacao);
    }

    @Test
    void deveGerarPdfPorPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(transacaoRepository.findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345", inicio, fim))
                .thenReturn(List.of(transacao));
        when(transacaoMapper.paraDTO(transacao)).thenReturn(transacaoDTO);

        byte[] resultado = extratoService.gerarPdfPorPeriodo("12345", inicio, fim);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        verify(transacaoRepository).findByNumeroContaAndDataHoraBetweenOrderByDataHoraDesc("12345", inicio, fim);
        verify(transacaoMapper).paraDTO(transacao);
    }

    @Test
    void deveGerarPdfMesmoSemTransacoes() {
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("99999"))
                .thenReturn(Collections.emptyList());

        byte[] resultado = extratoService.gerarPdf("99999");

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("99999");
        verify(transacaoMapper, never()).paraDTO(any());
    }

    @Test
    void deveLancarExcecaoQuandoRepositorioFalharNaGeracaoPdf() {
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("12345"))
                .thenThrow(new RuntimeException("Erro no banco"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> extratoService.gerarPdf("12345"));

        assertTrue(exception.getMessage().contains("Falha ao gerar extrato PDF"));

        verify(transacaoRepository).findByNumeroContaOrderByDataHoraDesc("12345");
    }
}