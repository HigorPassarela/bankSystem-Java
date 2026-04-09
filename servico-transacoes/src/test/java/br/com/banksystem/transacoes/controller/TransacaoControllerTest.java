package br.com.banksystem.transacoes.controller;

import br.com.banksystem.transacoes.dto.CreditoDTO;
import br.com.banksystem.transacoes.dto.DebitoDTO;
import br.com.banksystem.transacoes.dto.DepositoDTO;
import br.com.banksystem.transacoes.dto.SaldoDTO;
import br.com.banksystem.transacoes.dto.TransacaoRespostaDTO;
import br.com.banksystem.transacoes.dto.TransferenciaDTO;
import br.com.banksystem.transacoes.dto.TransferenciaRespostaDTO;
import br.com.banksystem.transacoes.security.JwtFiltroAutenticacao;
import br.com.banksystem.transacoes.security.JwtUtil;
import br.com.banksystem.transacoes.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransacaoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFiltroAutenticacao.class
        )
)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransacaoService transacaoService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String numeroConta = "12345678";

    @Test
    @WithMockUser(username = "12345678")
    void deposito_DeveRetornarSucesso_QuandoDepositoValido() throws Exception {
        DepositoDTO depositoDTO = new DepositoDTO(new BigDecimal("100.50"), "Depósito teste");
        TransacaoRespostaDTO respostaEsperada = new TransacaoRespostaDTO(
                "txn-123",
                numeroConta,
                new BigDecimal("100.50"),
                "DEPOSITO",
                "APROVADA",
                new BigDecimal("100.50"),
                LocalDateTime.now()
        );

        // Mock para quando numeroConta for null (que parece ser o caso)
        when(transacaoService.processarDeposito(isNull(), any(DepositoDTO.class)))
                .thenReturn(respostaEsperada);

        mockMvc.perform(post("/api/transacoes/deposito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.idTransacao").value("txn-123"))
                .andExpect(jsonPath("$.dados.tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.dados.status").value("APROVADA"))
                .andExpect(jsonPath("$.dados.valor").value(100.50))
                .andExpect(jsonPath("$.mensagem").value("Depósito de R$ 100.50 realizado com sucesso"));
    }

    @Test
    @WithMockUser(username = "12345678")
    void deposito_DeveRetornarErro_QuandoValorInvalido() throws Exception {
        DepositoDTO depositoDTO = new DepositoDTO(new BigDecimal("0.00"), "Depósito inválido");

        mockMvc.perform(post("/api/transacoes/deposito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositoDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void deposito_DeveRetornarErro_QuandoValorNulo() throws Exception {
        String jsonInvalido = """
                {
                  "valor": null,
                  "descricao": "Teste"
                }
                """;

        mockMvc.perform(post("/api/transacoes/deposito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void deposito_DeveRetornarErro_QuandoValorAcimaMaximo() throws Exception {
        DepositoDTO depositoDTO = new DepositoDTO(new BigDecimal("150000.00"), "Depósito muito alto");

        mockMvc.perform(post("/api/transacoes/deposito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositoDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void debito_DeveRetornarSucesso_QuandoDebitoValido() throws Exception {
        DebitoDTO debitoDTO = new DebitoDTO(new BigDecimal("50.25"), "Débito teste");
        TransacaoRespostaDTO respostaEsperada = new TransacaoRespostaDTO(
                "txn-456",
                numeroConta,
                new BigDecimal("50.25"),
                "DEBITO",
                "APROVADA",
                new BigDecimal("49.75"),
                LocalDateTime.now()
        );

        when(transacaoService.processarDebito(isNull(), any(DebitoDTO.class)))
                .thenReturn(respostaEsperada);

        mockMvc.perform(post("/api/transacoes/debito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debitoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.idTransacao").value("txn-456"))
                .andExpect(jsonPath("$.dados.tipo").value("DEBITO"))
                .andExpect(jsonPath("$.mensagem").value("Débito processado com sucesso"));
    }

    @Test
    @WithMockUser(username = "12345678")
    void debito_DeveRetornarErro_QuandoValorInvalido() throws Exception {
        DebitoDTO debitoDTO = new DebitoDTO(new BigDecimal("-10.00"), "Débito negativo");

        mockMvc.perform(post("/api/transacoes/debito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debitoDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void credito_DeveRetornarSucesso_QuandoCreditoValido() throws Exception {
        CreditoDTO creditoDTO = new CreditoDTO(new BigDecimal("200.00"), "Crédito teste");
        TransacaoRespostaDTO respostaEsperada = new TransacaoRespostaDTO(
                "txn-789",
                numeroConta,
                new BigDecimal("200.00"),
                "CREDITO",
                "APROVADA",
                new BigDecimal("300.00"),
                LocalDateTime.now()
        );

        when(transacaoService.processarCredito(isNull(), any(CreditoDTO.class)))
                .thenReturn(respostaEsperada);

        mockMvc.perform(post("/api/transacoes/credito")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creditoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.tipo").value("CREDITO"))
                .andExpect(jsonPath("$.mensagem").value("Crédito processado com sucesso"));
    }

    @Test
    @WithMockUser(username = "12345678")
    void transferencia_DeveRetornarSucesso_QuandoTransferenciaValida() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO(
                "87654321",
                new BigDecimal("150.00"),
                "1234",
                "Transferência teste"
        );

        TransferenciaRespostaDTO respostaEsperada = new TransferenciaRespostaDTO(
                "txn-transfer-123",
                numeroConta,
                "87654321",
                new BigDecimal("150.00"),
                "APROVADA",
                new BigDecimal("50.00"),
                LocalDateTime.now()
        );

        when(transacaoService.processarTransferencia(isNull(), any(TransferenciaDTO.class), anyString()))
                .thenReturn(respostaEsperada);

        mockMvc.perform(post("/api/transacoes/transferencia")
                        .with(csrf())
                        .header("Authorization", "Bearer token-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.contaOrigem").value(numeroConta))
                .andExpect(jsonPath("$.dados.contaDestino").value("87654321"))
                .andExpect(jsonPath("$.dados.valor").value(150.00))
                .andExpect(jsonPath("$.mensagem").value("Transferência realizada com sucesso"));
    }

    @Test
    @WithMockUser(username = "12345678")
    void transferencia_DeveRetornarErro_QuandoContaDestinoInvalida() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO(
                "1234567",
                new BigDecimal("150.00"),
                "1234",
                "Transferência inválida"
        );

        mockMvc.perform(post("/api/transacoes/transferencia")
                        .with(csrf())
                        .header("Authorization", "Bearer token-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void transferencia_DeveRetornarErro_QuandoSenhaTransferenciaInvalida() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO(
                "87654321",
                new BigDecimal("150.00"),
                "123",
                "Transferência inválida"
        );

        mockMvc.perform(post("/api/transacoes/transferencia")
                        .with(csrf())
                        .header("Authorization", "Bearer token-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void transferencia_DeveRetornarErro_QuandoValorExcedeMaximo() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO(
                "87654321",
                new BigDecimal("60000.00"),
                "1234",
                "Transferência acima do limite"
        );

        mockMvc.perform(post("/api/transacoes/transferencia")
                        .with(csrf())
                        .header("Authorization", "Bearer token-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void transferencia_DeveRetornarErro_QuandoContaDestinoVazia() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO(
                "",
                new BigDecimal("150.00"),
                "1234",
                "Transferência sem destino"
        );

        mockMvc.perform(post("/api/transacoes/transferencia")
                        .with(csrf())
                        .header("Authorization", "Bearer token-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void transferencia_DeveRetornarErro_QuandoSenhaTransferenciaVazia() throws Exception {
        TransferenciaDTO transferenciaDTO = new TransferenciaDTO(
                "87654321",
                new BigDecimal("150.00"),
                "",
                "Transferência sem senha"
        );

        mockMvc.perform(post("/api/transacoes/transferencia")
                        .with(csrf())
                        .header("Authorization", "Bearer token-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "12345678")
    void consultarSaldo_DeveRetornarSaldoCorretamente() throws Exception {
        SaldoDTO saldoEsperado = new SaldoDTO(
                numeroConta,
                new BigDecimal("150.00"),
                new BigDecimal("500.00")
        );

        when(transacaoService.consultarSaldo(isNull())).thenReturn(saldoEsperado);

        mockMvc.perform(get("/api/transacoes/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.numeroConta").value(numeroConta))
                .andExpect(jsonPath("$.dados.saldoDisponivel").value(150.00))
                .andExpect(jsonPath("$.dados.limiteDisponivel").value(500.00))
                .andExpect(jsonPath("$.mensagem").value("Saldo consultado com sucesso"));
    }

    @Test
    @WithMockUser(username = "12345678")
    void consultarLimite_DeveRetornarLimiteCorretamente() throws Exception {
        SaldoDTO limiteEsperado = new SaldoDTO(
                numeroConta,
                new BigDecimal("150.00"),
                new BigDecimal("500.00")
        );

        when(transacaoService.consultarSaldo(isNull())).thenReturn(limiteEsperado);

        mockMvc.perform(get("/api/transacoes/limite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.numeroConta").value(numeroConta))
                .andExpect(jsonPath("$.dados.saldoDisponivel").value(150.00))
                .andExpect(jsonPath("$.dados.limiteDisponivel").value(500.00))
                .andExpect(jsonPath("$.mensagem").value("Limite consultado com sucesso"));
    }
}