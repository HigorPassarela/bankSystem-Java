package br.com.banksystem.extratos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFiltroAutenticacaoTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFiltroAutenticacao jwtFiltroAutenticacao;

    @BeforeEach
    void setUp() {
        jwtFiltroAutenticacao = new JwtFiltroAutenticacao(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarComTokenValido() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        String numeroConta = "123456789";
        String authorizationHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtil.validarToken(token)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(token)).thenReturn(numeroConta);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(numeroConta, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USUARIO")));
        assertEquals(1, authentication.getAuthorities().size());

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validarToken(token);
        verify(jwtUtil).extrairNumeroConta(token);
    }

    @Test
    void naoDeveAutenticarComTokenInvalido() throws ServletException, IOException {
        // Arrange
        String token = "invalid-jwt-token";
        String authorizationHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtil.validarToken(token)).thenReturn(false);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validarToken(token);
        verify(jwtUtil, never()).extrairNumeroConta(token);
    }

    @Test
    void naoDeveAutenticarSemHeaderAuthorization() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validarToken(any());
        verify(jwtUtil, never()).extrairNumeroConta(any());
    }

    @Test
    void naoDeveAutenticarComHeaderSemBearer() throws ServletException, IOException {
        // Arrange
        String authorizationHeader = "Basic some-basic-auth";
        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validarToken(any());
        verify(jwtUtil, never()).extrairNumeroConta(any());
    }

    @Test
    void naoDeveAutenticarQuandoNumeroContaEhNull() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        String authorizationHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtil.validarToken(token)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(token)).thenReturn(null);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validarToken(token);
        verify(jwtUtil).extrairNumeroConta(token);
    }

    @Test
    void naoDeveAutenticarSeJaExisteAutenticacao() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        String numeroConta = "123456789";
        String authorizationHeader = "Bearer " + token;

        // Configurar autenticação existente
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtil.validarToken(token)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(token)).thenReturn(numeroConta);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(existingAuth, authentication); // Deve manter a autenticação existente

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validarToken(token);
        verify(jwtUtil).extrairNumeroConta(token);
    }

    @Test
    void deveProcessarTokenVazio() throws ServletException, IOException {
        // Arrange
        String authorizationHeader = "Bearer ";
        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtil.validarToken("")).thenReturn(false);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).validarToken("");
        verify(jwtUtil, never()).extrairNumeroConta(any());
    }

    @Test
    void deveProcessarHeaderComApenasBearer() throws ServletException, IOException {
        // Arrange
        String authorizationHeader = "Bearer";
        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        // Act
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validarToken(any());
        verify(jwtUtil, never()).extrairNumeroConta(any());
    }

    @Test
    void deveChamarFilterChainMesmoComExcecao() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        String authorizationHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);
        when(jwtUtil.validarToken(token)).thenThrow(new RuntimeException("Erro na validação"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
        });

        verify(jwtUtil).validarToken(token);
        verify(filterChain, never()).doFilter(request, response);
    }
}