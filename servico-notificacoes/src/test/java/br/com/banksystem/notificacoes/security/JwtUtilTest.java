package br.com.banksystem.notificacoes.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SEGREDO = "meu-segredo-super-seguro-com-pelo-menos-32-bytes";
    private static final String NUMERO_CONTA = "12345-6";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "segredo", SEGREDO);
    }

    @Test
    @DisplayName("Deve extrair número da conta de token válido")
    void deveExtrairNumeroContaDeTokenValido() {
        // Given
        String token = gerarTokenValido(NUMERO_CONTA);

        // When
        String numeroContaExtraido = jwtUtil.extrairNumeroConta(token);

        // Then
        assertThat(numeroContaExtraido).isEqualTo(NUMERO_CONTA);
    }

    @Test
    @DisplayName("Deve validar token válido")
    void deveValidarTokenValido() {
        // Given
        String token = gerarTokenValido(NUMERO_CONTA);

        // When
        boolean valido = jwtUtil.validarToken(token);

        // Then
        assertThat(valido).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso para token inválido")
    void deveRetornarFalsoParaTokenInvalido() {
        // Given
        String tokenInvalido = "token.invalido.qualquer";

        // When
        boolean valido = jwtUtil.validarToken(tokenInvalido);

        // Then
        assertThat(valido).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso para token assinado com outro segredo")
    void deveRetornarFalsoParaTokenAssinadoComOutroSegredo() {
        // Given
        String outroSegredo = "outro-segredo-super-seguro-com-pelo-menos-32-bytes";
        SecretKey outraChave = io.jsonwebtoken.security.Keys.hmacShaKeyFor(outroSegredo.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(NUMERO_CONTA)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(outraChave)
                .compact();

        // When
        boolean valido = jwtUtil.validarToken(token);

        // Then
        assertThat(valido).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso para token expirado")
    void deveRetornarFalsoParaTokenExpirado() {
        // Given
        SecretKey chave = io.jsonwebtoken.security.Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8));

        String tokenExpirado = Jwts.builder()
                .subject(NUMERO_CONTA)
                .issuedAt(new Date(System.currentTimeMillis() - 120000))
                .expiration(new Date(System.currentTimeMillis() - 60000))
                .signWith(chave)
                .compact();

        // When
        boolean valido = jwtUtil.validarToken(tokenExpirado);

        // Then
        assertThat(valido).isFalse();
    }

    private String gerarTokenValido(String numeroConta) {
        SecretKey chave = io.jsonwebtoken.security.Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(numeroConta)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(chave)
                .compact();
    }
}