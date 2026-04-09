package br.com.banksystem.contas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para JwtFiltroAutenticacao")
class JwtFiltroAutenticacaoTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ContaUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtFiltroAutenticacao jwtFiltroAutenticacao;

    private final String tokenValido = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private final String numeroConta = "12345678";
    private final String bearerToken = "Bearer " + tokenValido;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User(
                numeroConta,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("Deve autenticar usuário com token válido")
    void deveAutenticarUsuarioComTokenValido() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtUtil.validarToken(tokenValido)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(tokenValido)).thenReturn(numeroConta);
        when(userDetailsService.loadUserByUsername(numeroConta)).thenReturn(userDetails);

        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(securityContext.getAuthentication()).thenReturn(null);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

            // Then
            verify(jwtUtil).validarToken(tokenValido);
            verify(jwtUtil).extrairNumeroConta(tokenValido);
            verify(userDetailsService).loadUserByUsername(numeroConta);
            verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticação quando não há cabeçalho Authorization")
    void deveProsseguirSemAutenticacaoQuandoNaoHaCabecalhoAuthorization() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).validarToken(anyString());
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticação quando cabeçalho não inicia com Bearer")
    void deveProsseguirSemAutenticacaoQuandoCabecalhoNaoIniciaComBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        // When
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).validarToken(anyString());
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticação quando token é inválido")
    void deveProsseguirSemAutenticacaoQuandoTokenEInvalido() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtUtil.validarToken(tokenValido)).thenReturn(false);

        // When
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).validarToken(tokenValido);
        verify(jwtUtil, never()).extrairNumeroConta(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticação quando número da conta é nulo")
    void deveProsseguirSemAutenticacaoQuandoNumeroContaENulo() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtUtil.validarToken(tokenValido)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(tokenValido)).thenReturn(null);

        // When
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).validarToken(tokenValido);
        verify(jwtUtil).extrairNumeroConta(tokenValido);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticação quando usuário já está autenticado")
    void deveProsseguirSemAutenticacaoQuandoUsuarioJaEstaAutenticado() throws ServletException, IOException {
        // Given
        Authentication authenticationExistente = mock(Authentication.class);
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtUtil.validarToken(tokenValido)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(tokenValido)).thenReturn(numeroConta);

        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(securityContext.getAuthentication()).thenReturn(authenticationExistente);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

            // Then
            verify(jwtUtil).validarToken(tokenValido);
            verify(jwtUtil).extrairNumeroConta(tokenValido);
            verify(userDetailsService, never()).loadUserByUsername(anyString());
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Deve prosseguir quando token Bearer está vazio")
    void deveProsseguirQuandoTokenBearerEstaVazio() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).validarToken("");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve propagar exceção do UserDetailsService")
    void devePropagaExcecaoDoUserDetailsService() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtUtil.validarToken(tokenValido)).thenReturn(true);
        when(jwtUtil.extrairNumeroConta(tokenValido)).thenReturn(numeroConta);
        when(userDetailsService.loadUserByUsername(numeroConta))
                .thenThrow(new RuntimeException("Erro ao carregar usuário"));

        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(securityContext.getAuthentication()).thenReturn(null);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When & Then - A exceção deve ser propagada
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                jwtFiltroAutenticacao.doFilterInternal(request, response, filterChain);
            });

            // Then
            verify(jwtUtil).validarToken(tokenValido);
            verify(jwtUtil).extrairNumeroConta(tokenValido);
            verify(userDetailsService).loadUserByUsername(numeroConta);
            verify(securityContext, never()).setAuthentication(any());
            // filterChain.doFilter não deve ser chamado devido à exceção
        }
    }
}