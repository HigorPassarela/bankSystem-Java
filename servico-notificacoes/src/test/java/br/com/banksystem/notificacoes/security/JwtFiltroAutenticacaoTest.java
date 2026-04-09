package br.com.banksystem.notificacoes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtFiltroAutenticacao")
class JwtFiltroAutenticacaoTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private JwtFiltroAutenticacao jwtFiltroAutenticacao;

    @BeforeEach
    void setUp() {
        jwtFiltroAutenticacao = new JwtFiltroAutenticacao(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar usuário quando token for válido")
    void deveAutenticarUsuarioQuandoTokenForValido() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer token-valido");

        when(jwtUtil.validarToken("token-valido")).thenReturn(true);
        when(jwtUtil.extrairNumeroConta("token-valido")).thenReturn("12345-6");

        // When
        jwtFiltroAutenticacao.doFilter(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("12345-6");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USUARIO");

        verify(jwtUtil).validarToken("token-valido");
        verify(jwtUtil).extrairNumeroConta("token-valido");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando header Authorization não existir")
    void naoDeveAutenticarQuandoHeaderAuthorizationNaoExistir() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        jwtFiltroAutenticacao.doFilter(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtUtil, never()).validarToken(anyString());
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando header não começar com Bearer")
    void naoDeveAutenticarQuandoHeaderNaoComecarComBearer() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic abc123");

        // When
        jwtFiltroAutenticacao.doFilter(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtUtil, never()).validarToken(anyString());
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando token for inválido")
    void naoDeveAutenticarQuandoTokenForInvalido() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer token-invalido");

        when(jwtUtil.validarToken("token-invalido")).thenReturn(false);

        // When
        jwtFiltroAutenticacao.doFilter(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtUtil).validarToken("token-invalido");
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando número da conta for nulo")
    void naoDeveAutenticarQuandoNumeroContaForNulo() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer token-valido");

        when(jwtUtil.validarToken("token-valido")).thenReturn(true);
        when(jwtUtil.extrairNumeroConta("token-valido")).thenReturn(null);

        // When
        jwtFiltroAutenticacao.doFilter(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtUtil).validarToken("token-valido");
        verify(jwtUtil).extrairNumeroConta("token-valido");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve sobrescrever autenticação já existente no contexto")
    void naoDeveSobrescreverAutenticacaoJaExistenteNoContexto() throws ServletException, IOException {
        // Given
        var authExistente = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "usuario-existente", null, java.util.List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authExistente);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer token-valido");

        when(jwtUtil.validarToken("token-valido")).thenReturn(true);
        when(jwtUtil.extrairNumeroConta("token-valido")).thenReturn("12345-6");

        // When
        jwtFiltroAutenticacao.doFilter(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("usuario-existente");

        verify(jwtUtil).validarToken("token-valido");
        verify(jwtUtil).extrairNumeroConta("token-valido");
        verify(filterChain).doFilter(request, response);
    }
}