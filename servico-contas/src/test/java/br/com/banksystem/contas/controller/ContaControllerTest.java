package br.com.banksystem.contas.controller;

import br.com.banksystem.contas.dto.*;
import br.com.banksystem.contas.service.ContaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContaController.class)
@DisplayName("Testes do ContaController")
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContaService contaService;

    @Test
    @DisplayName("Deve criar conta com sucesso")
    void deveCriarContaComSucesso() throws Exception {
        CriarContaDTO dto = new CriarContaDTO(
                "João Silva", "12345678901", "joao@email.com", "11987654321", "senha123"
        );
        PerfilContaDTO perfil = new PerfilContaDTO(
                "12345678", "João Silva", "12345678901", "joao@email.com",
                "11987654321", true, LocalDateTime.now()
        );

        when(contaService.criarConta(any())).thenReturn(perfil);

        mockMvc.perform(post("/api/contas/criar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.numeroConta").value("12345678"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar conta com dados inválidos")
    void deveRetornarErroComDadosInvalidos() throws Exception {
        CriarContaDTO dto = new CriarContaDTO("", "", "email-invalido", "", "123");

        mockMvc.perform(post("/api/contas/criar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void deveRealizarLoginComSucesso() throws Exception {
        LoginDTO dto = new LoginDTO("12345678", "senha123");
        TokenDTO token = new TokenDTO("jwt.token.aqui", "Bearer", "12345678", "João Silva", 86400000L);

        when(contaService.autenticar(any())).thenReturn(token);

        mockMvc.perform(post("/api/contas/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.tipo").value("Bearer"));
    }

    @Test
    @DisplayName("Deve retornar perfil para usuário autenticado")
    @WithMockUser(username = "12345678")
    void deveRetornarPerfilAutenticado() throws Exception {
        PerfilContaDTO perfil = new PerfilContaDTO(
                "12345678", "João Silva", "12345678901", "joao@email.com",
                "11987654321", true, LocalDateTime.now()
        );

        when(contaService.obterPerfil("12345678")).thenReturn(perfil);

        mockMvc.perform(get("/api/contas/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.nomeCompleto").value("João Silva"));
    }
}
