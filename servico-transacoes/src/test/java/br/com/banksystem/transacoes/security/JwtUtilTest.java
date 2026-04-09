package br.com.banksystem.transacoes.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secretKey;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        secretKey = "minha-chave-secreta-super-segura-com-pelo-menos-32-caracteres";
        ReflectionTestUtils.setField(jwtUtil, "segredo", secretKey);
        signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void deveExtrairNumeroContaComSucesso() {
        String numeroConta = "123456789";
        String token = criarTokenValido(numeroConta);

        String resultado = jwtUtil.extrairNumeroConta(token);

        assertEquals(numeroConta, resultado);
    }

    @Test
    void deveValidarTokenValidoComSucesso() {
        String token = criarTokenValido("123456789");

        boolean resultado = jwtUtil.validarToken(token);

        assertTrue(resultado);
    }

    @Test
    void deveRetornarFalsoParaTokenInvalido() {
        String tokenInvalido = "token.invalido.aqui";

        boolean resultado = jwtUtil.validarToken(tokenInvalido);

        assertFalse(resultado);
    }

    @Test
    void deveRetornarFalsoParaTokenVazio() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.validarToken("");
        });
    }

    @Test
    void deveRetornarFalsoParaTokenNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.validarToken(null);
        });
    }

    @Test
    void deveRetornarFalsoParaTokenExpirado() {
        String numeroConta = "123456789";
        String tokenExpirado = criarTokenExpirado(numeroConta);

        boolean resultado = jwtUtil.validarToken(tokenExpirado);

        assertFalse(resultado);
    }

    @Test
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenInvalido() {
        String tokenInvalido = "token.invalido.aqui";

        assertThrows(JwtException.class, () -> {
            jwtUtil.extrairNumeroConta(tokenInvalido);
        });
    }

    @Test
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extrairNumeroConta(null);
        });
    }

    @Test
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenVazio() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extrairNumeroConta("");
        });
    }

    @Test
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenExpirado() {
        String numeroConta = "123456789";
        String tokenExpirado = criarTokenExpirado(numeroConta);

        assertThrows(JwtException.class, () -> {
            jwtUtil.extrairNumeroConta(tokenExpirado);
        });
    }

    @Test
    void deveValidarTokenComDiferentesNumerosDeConta() {
        String[] numerosConta = {"123456789", "987654321", "555666777", "111222333"};

        for (String numeroConta : numerosConta) {
            String token = criarTokenValido(numeroConta);
            boolean valido = jwtUtil.validarToken(token);
            String numeroExtraido = jwtUtil.extrairNumeroConta(token);

            assertTrue(valido, "Token deve ser válido para conta: " + numeroConta);
            assertEquals(numeroConta, numeroExtraido, "Número da conta deve ser extraído corretamente");
        }
    }

    @Test
    void deveRetornarFalsoParaTokenComAssinaturaInvalida() {
        String numeroConta = "123456789";
        String chaveIncorreta = "chave-incorreta-para-gerar-token-com-assinatura-invalida";
        SecretKey chaveIncorretaKey = Keys.hmacShaKeyFor(chaveIncorreta.getBytes(StandardCharsets.UTF_8));

        String tokenComAssinaturaInvalida = Jwts.builder()
                .subject(numeroConta)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(chaveIncorretaKey)
                .compact();

        boolean resultado = jwtUtil.validarToken(tokenComAssinaturaInvalida);

        assertFalse(resultado);
    }

    @Test
    void deveLancarExcecaoParaTokenComAssinaturaInvalidaNaExtracao() {
        String numeroConta = "123456789";
        String chaveIncorreta = "chave-incorreta-para-gerar-token-com-assinatura-invalida";
        SecretKey chaveIncorretaKey = Keys.hmacShaKeyFor(chaveIncorreta.getBytes(StandardCharsets.UTF_8));

        String tokenComAssinaturaInvalida = Jwts.builder()
                .subject(numeroConta)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(chaveIncorretaKey)
                .compact();

        assertThrows(JwtException.class, () -> {
            jwtUtil.extrairNumeroConta(tokenComAssinaturaInvalida);
        });
    }

    @Test
    void deveValidarTokenSemDataDeExpiracao() {
        String numeroConta = "123456789";
        String tokenSemExpiracao = Jwts.builder()
                .subject(numeroConta)
                .issuedAt(new Date())
                .signWith(signingKey)
                .compact();

        boolean valido = jwtUtil.validarToken(tokenSemExpiracao);
        String numeroExtraido = jwtUtil.extrairNumeroConta(tokenSemExpiracao);

        assertTrue(valido);
        assertEquals(numeroConta, numeroExtraido);
    }

    @Test
    void deveExtrairNumeroContaComCaracteresEspeciais() {
        String numeroContaEspecial = "12345-6789";
        String token = criarTokenValido(numeroContaEspecial);

        String resultado = jwtUtil.extrairNumeroConta(token);

        assertEquals(numeroContaEspecial, resultado);
    }

    @Test
    void deveRetornarFalsoParaTokenComEspacosEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.validarToken("   ");
        });
    }

    @Test
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenComEspacosEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extrairNumeroConta("   ");
        });
    }

    @Test
    void deveRetornarFalsoParaTokenMalformado() {
        String tokenMalformado = "abc123";

        boolean resultado = jwtUtil.validarToken(tokenMalformado);

        assertFalse(resultado);
    }

    @Test
    void deveLancarExcecaoParaTokenMalformadoNaExtracao() {
        String tokenMalformado = "abc123";

        assertThrows(JwtException.class, () -> {
            jwtUtil.extrairNumeroConta(tokenMalformado);
        });
    }

    private String criarTokenValido(String numeroConta) {
        return Jwts.builder()
                .subject(numeroConta)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(signingKey)
                .compact();
    }

    private String criarTokenExpirado(String numeroConta) {
        return Jwts.builder()
                .subject(numeroConta)
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(signingKey)
                .compact();
    }
}