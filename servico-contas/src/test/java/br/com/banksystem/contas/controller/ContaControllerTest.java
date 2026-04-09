package br.com.banksystem.contas.controller;

import br.com.banksystem.contas.dto.*;
import br.com.banksystem.contas.exception.GlobalExceptionHandler;
import br.com.banksystem.contas.model.dto.StatusConta;
import br.com.banksystem.contas.service.ContaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ContaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContaService contaService;

    @InjectMocks
    private ContaController contaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(contaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveCriarContaComSucesso() throws Exception {
        CriarContaDTO dto = new CriarContaDTO(
                "João Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                "senha123",
                "1234"
        );

        PerfilContaDTO perfil = new PerfilContaDTO(
                "12345678",
                "João Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                StatusConta.PENDENTE_EMAIL,
                false,
                false,
                LocalDateTime.now()
        );

        when(contaService.criarConta(any(CriarContaDTO.class))).thenReturn(perfil);

        mockMvc.perform(post("/api/contas/criar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.numeroConta").value("12345678"))
                .andExpect(jsonPath("$.dados.nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$.mensagem").value("Conta criada! Verifique seu e-mail para ativar a conta."));

        verify(contaService).criarConta(any(CriarContaDTO.class));
    }

    @Test
    void deveVerificarEmailComSucesso() throws Exception {
        VerificarEmailDTO resultado = new VerificarEmailDTO(
                "E-mail verificado com sucesso! Conta 12345678 está ATIVA.",
                true
        );

        when(contaService.verificarEmail("token123")).thenReturn(resultado);

        mockMvc.perform(get("/api/contas/verificar-email")
                        .param("token", "token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.verificado").value(true))
                .andExpect(jsonPath("$.mensagem").value("E-mail verificado com sucesso! Conta 12345678 está ATIVA."));

        verify(contaService).verificarEmail("token123");
    }

    @Test
    void deveReenviarVerificacaoComSucesso() throws Exception {
        doNothing().when(contaService).reenviarVerificacao("joao@email.com");

        mockMvc.perform(post("/api/contas/reenviar-verificacao")
                        .param("email", "joao@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").value("E-mail de verificação reenviado com sucesso"));

        verify(contaService).reenviarVerificacao("joao@email.com");
    }

    @Test
    void deveRealizarLoginComSucesso() throws Exception {
        LoginDTO dto = new LoginDTO("12345678", "senha123");

        TokenDTO tokenDTO = new TokenDTO(
                "jwt-token",
                "Bearer",
                "12345678",
                "João Silva",
                86400000L
        );

        when(contaService.autenticar(any(LoginDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/api/contas/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.token").value("jwt-token"))
                .andExpect(jsonPath("$.dados.numeroConta").value("12345678"))
                .andExpect(jsonPath("$.dados.nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$.mensagem").value("Login realizado com sucesso"));

        verify(contaService).autenticar(any(LoginDTO.class));
    }

    @Test
    void deveObterPerfilComSucesso() {
        PerfilContaDTO perfil = new PerfilContaDTO(
                "12345678",
                "João Silva",
                "12345678901",
                "joao@email.com",
                "11999999999",
                StatusConta.ATIVA,
                true,
                true,
                LocalDateTime.now()
        );

        when(contaService.obterPerfil("12345678")).thenReturn(perfil);

        UserDetails userDetails = User.withUsername("12345678")
                .password("senha")
                .authorities(List.of())
                .build();

        var response = contaController.obterPerfil(userDetails);

        verify(contaService).obterPerfil("12345678");
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().dados().numeroConta().equals("12345678");
    }

    @Test
    void deveAtualizarContaComSucesso() {
        AtualizarContaDTO dto = new AtualizarContaDTO(
                "João Atualizado",
                "novo@email.com",
                "11988887777",
                "novaSenha123"
        );

        PerfilContaDTO perfilAtualizado = new PerfilContaDTO(
                "12345678",
                "João Atualizado",
                "12345678901",
                "novo@email.com",
                "11988887777",
                StatusConta.ATIVA,
                true,
                true,
                LocalDateTime.now()
        );

        when(contaService.atualizarConta(eq("12345678"), any(AtualizarContaDTO.class)))
                .thenReturn(perfilAtualizado);

        UserDetails userDetails = User.withUsername("12345678")
                .password("senha")
                .authorities(List.of())
                .build();

        var response = contaController.atualizarConta(userDetails, dto);

        verify(contaService).atualizarConta(eq("12345678"), any(AtualizarContaDTO.class));
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().dados().nomeCompleto().equals("João Atualizado");
    }

    @Test
    void deveAtualizarSenhaTransferenciaComSucesso() {
        AtualizarSenhaTransferenciaDTO dto = new AtualizarSenhaTransferenciaDTO("1234", "5678");

        doNothing().when(contaService)
                .atualizarSenhaTransferencia(eq("12345678"), any(AtualizarSenhaTransferenciaDTO.class));

        UserDetails userDetails = User.withUsername("12345678")
                .password("senha")
                .authorities(List.of())
                .build();

        var response = contaController.atualizarSenhaTransferencia(userDetails, dto);

        verify(contaService).atualizarSenhaTransferencia(eq("12345678"), any(AtualizarSenhaTransferenciaDTO.class));
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().mensagem().equals("Senha de transferência atualizada com sucesso");
    }

    @Test
    void deveBuscarContaPorNumeroComSucesso() throws Exception {
        PerfilContaDTO perfil = new PerfilContaDTO(
                "87654321",
                "Maria Souza",
                "98765432100",
                "maria@email.com",
                "11977776666",
                StatusConta.ATIVA,
                true,
                true,
                LocalDateTime.now()
        );

        when(contaService.buscarContaPorNumero("87654321")).thenReturn(perfil);

        mockMvc.perform(get("/api/contas/buscar/87654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.numeroConta").value("87654321"))
                .andExpect(jsonPath("$.dados.nomeCompleto").value("Maria Souza"))
                .andExpect(jsonPath("$.mensagem").value("Conta encontrada"));

        verify(contaService).buscarContaPorNumero("87654321");
    }

    @Test
    void deveValidarSenhaTransferenciaComSucesso() {
        when(contaService.validarSenhaTransferencia("12345678", "1234")).thenReturn(true);

        UserDetails userDetails = User.withUsername("12345678")
                .password("senha")
                .authorities(List.of())
                .build();

        var response = contaController.validarSenhaTransferencia(userDetails, "1234");

        verify(contaService).validarSenhaTransferencia("12345678", "1234");
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().dados();
    }
}