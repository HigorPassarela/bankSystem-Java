package br.com.banksystem.extratos.service;

import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.mapper.TransacaoMapper;
import br.com.banksystem.extratos.model.Transacao;
import br.com.banksystem.extratos.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ExtratoService")
class ExtratoServiceTest {

    @Mock private TransacaoRepository transacaoRepository;
    @Mock private TransacaoMapper transacaoMapper;
    @InjectMocks private ExtratoService extratoService;

    @Test
    @DisplayName("Deve listar transações por conta")
    void deveListarTransacoesPorConta() {
        Transacao t = new Transacao("id1","00001234","txn-uuid",new BigDecimal("100"),
                "DEBITO","APROVADA","Teste", LocalDateTime.now());
        TransacaoDTO dto = new TransacaoDTO("txn-uuid","00001234",new BigDecimal("100"),
                "DEBITO","APROVADA","Teste",LocalDateTime.now());
        when(transacaoRepository.findByNumeroContaOrderByDataHoraDesc("00001234")).thenReturn(List.of(t));
        when(transacaoMapper.paraDTO(t)).thenReturn(dto);
        List<TransacaoDTO> resultado = extratoService.listarPorConta("00001234");
        assertEquals(1, resultado.size());
        assertEquals("txn-uuid", resultado.get(0).idTransacao());
    }
}
