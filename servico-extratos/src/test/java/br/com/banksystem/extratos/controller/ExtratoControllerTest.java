package br.com.banksystem.extratos.controller;

import br.com.banksystem.extratos.dto.TransacaoDTO;
import br.com.banksystem.extratos.service.ExtratoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExtratoController.class)
@ContextConfiguration(classes = {ExtratoController.class, ExtratoControllerTest.TestSecurityConfig.class})
@ExtendWith(MockitoExtension.class)
class ExtratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExtratoService extratoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                    .csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    private TransacaoDTO criarTransacaoDTO() {
        return new TransacaoDTO(
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
    void deveListarTransacoesPorConta() throws Exception {
        // Given
        String numeroConta = "12345";
        List<TransacaoDTO> transacoes = Arrays.asList(criarTransacaoDTO());
        when(extratoService.listarPorConta(numeroConta)).thenReturn(transacoes);

        // When & Then
        mockMvc.perform(get("/api/extratos/conta/{numeroConta}", numeroConta))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").isArray())
                .andExpect(jsonPath("$.dados[0].idTransacao").value("TXN-001"))
                .andExpect(jsonPath("$.dados[0].numeroConta").value("12345"))
                .andExpect(jsonPath("$.dados[0].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.mensagem").value("1 transação(ões) encontrada(s)"));
    }

    @Test
    void deveListarTransacoesPaginadas() throws Exception {
        // Given
        String numeroConta = "12345";
        List<TransacaoDTO> transacoes = Arrays.asList(criarTransacaoDTO());
        Pageable pageable = PageRequest.of(0, 20);
        Page<TransacaoDTO> pagina = new PageImpl<>(transacoes, pageable, 1); // Corrigido: adicionado total
        when(extratoService.listarPorContaPaginado(eq(numeroConta), eq(0), eq(20)))
                .thenReturn(pagina);

        // When & Then
        mockMvc.perform(get("/api/extratos/conta/{numeroConta}/paginado", numeroConta)
                        .param("pagina", "0")
                        .param("tamanho", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.content").isArray())
                .andExpect(jsonPath("$.dados.totalElements").value(1))
                .andExpect(jsonPath("$.mensagem").value("Página 1 de 1"));
    }

    @Test
    void deveListarTransacoesPorPeriodo() throws Exception {
        // Given
        String numeroConta = "12345";
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2024, 1, 31, 23, 59);
        List<TransacaoDTO> transacoes = Arrays.asList(criarTransacaoDTO());

        when(extratoService.listarPorPeriodo(eq(numeroConta), eq(inicio), eq(fim)))
                .thenReturn(transacoes);

        // When & Then
        mockMvc.perform(get("/api/extratos/periodo")
                        .param("numeroConta", numeroConta)
                        .param("inicio", "2024-01-01T00:00:00")
                        .param("fim", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").isArray())
                .andExpect(jsonPath("$.dados[0].idTransacao").value("TXN-001"))
                .andExpect(jsonPath("$.mensagem").value("1 transação(ões) no período"));
    }

    @Test
    void deveListarTransacoesPorTipo() throws Exception {
        // Given
        String numeroConta = "12345";
        String tipo = "DEPOSITO";
        List<TransacaoDTO> transacoes = Arrays.asList(criarTransacaoDTO());

        when(extratoService.listarPorTipo(numeroConta, tipo)).thenReturn(transacoes);

        // When & Then
        mockMvc.perform(get("/api/extratos/tipo")
                        .param("numeroConta", numeroConta)
                        .param("tipo", tipo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").isArray())
                .andExpect(jsonPath("$.dados[0].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.mensagem").value("1 transação(ões) do tipo DEPOSITO"));
    }

    @Test
    void deveGerarPdfCompleto() throws Exception {
        // Given
        String numeroConta = "12345";
        byte[] pdfBytes = "PDF content".getBytes();
        when(extratoService.gerarPdf(numeroConta)).thenReturn(pdfBytes);

        // When & Then
        mockMvc.perform(get("/api/extratos/pdf/{numeroConta}", numeroConta))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=extrato-12345.pdf"))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void deveGerarPdfPorPeriodo() throws Exception {
        // Given
        String numeroConta = "12345";
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2024, 1, 31, 23, 59);
        byte[] pdfBytes = "PDF content".getBytes();

        when(extratoService.gerarPdfPorPeriodo(eq(numeroConta), eq(inicio), eq(fim)))
                .thenReturn(pdfBytes);

        // When & Then
        mockMvc.perform(get("/api/extratos/pdf/{numeroConta}/periodo", numeroConta)
                        .param("inicio", "2024-01-01T00:00:00")
                        .param("fim", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=extrato-12345-2024-01-01-a-2024-01-31.pdf"))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverTransacoes() throws Exception {
        // Given
        String numeroConta = "99999";
        when(extratoService.listarPorConta(numeroConta)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/extratos/conta/{numeroConta}", numeroConta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados").isEmpty())
                .andExpect(jsonPath("$.mensagem").value("0 transação(ões) encontrada(s)"));
    }
}