package br.com.banksystem.contas.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String segredoTeste = "meuSegredoSuperSecreto123456789012345678901234567890";
    private final long expiracaoMsTeste = 3600000L; // 1 hora
    private final String numeroConta = "12345678";
    private final String nomeCompleto = "João Silva";
    private final String role = "ROLE_USER";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "segredo", segredoTeste);
        ReflectionTestUtils.setField(jwtUtil, "expiracaoMs", expiracaoMsTeste);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido com todas as claims")
    void deveGerarTokenJwtValidoComTodasAsClaims() {
        // When
        String token = jwtUtil.gerarToken(numeroConta, nomeCompleto, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature

        // Verifica se o token pode ser parseado
        SecretKey chave = Keys.hmacShaKeyFor(segredoTeste.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(numeroConta);
        assertThat(claims.get("nomeCompleto", String.class)).isEqualTo(nomeCompleto);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration().getTime())
                .isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("Deve extrair número da conta do token válido")
    void deveExtrairNumeroContaDoTokenValido() {
        // Given
        String token = jwtUtil.gerarToken(numeroConta, nomeCompleto, role);

        // When
        String numeroContaExtraido = jwtUtil.extrairNumeroConta(token);

        // Then
        assertThat(numeroContaExtraido).isEqualTo(numeroConta);
    }

    @Test
    @DisplayName("Deve validar token válido")
    void deveValidarTokenValido() {
        // Given
        String token = jwtUtil.gerarToken(numeroConta, nomeCompleto, role);

        // When
        boolean tokenValido = jwtUtil.validarToken(token);

        // Then
        assertThat(tokenValido).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para token malformado")
    void deveRetornarFalseParaTokenMalformado() {
        // Given
        String tokenMalformado = "token.malformado.invalido";

        // When
        boolean tokenValido = jwtUtil.validarToken(tokenMalformado);

        // Then
        assertThat(tokenValido).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false para token com assinatura inválida")
    void deveRetornarFalseParaTokenComAssinaturaInvalida() {
        // Given
        String segredoIncorreto = "segredoIncorreto123456789012345678901234567890";
        SecretKey chaveIncorreta = Keys.hmacShaKeyFor(segredoIncorreto.getBytes(StandardCharsets.UTF_8));

        String tokenComAssinaturaInvalida = Jwts.builder()
                .subject(numeroConta)
                .claim("nomeCompleto", nomeCompleto)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracaoMsTeste))
                .signWith(chaveIncorreta)
                .compact();

        // When
        boolean tokenValido = jwtUtil.validarToken(tokenComAssinaturaInvalida);

        // Then
        assertThat(tokenValido).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false para token expirado")
    void deveRetornarFalseParaTokenExpirado() {
        // Given
        SecretKey chave = Keys.hmacShaKeyFor(segredoTeste.getBytes(StandardCharsets.UTF_8));
        String tokenExpirado = Jwts.builder()
                .subject(numeroConta)
                .claim("nomeCompleto", nomeCompleto)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 horas atrás
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hora atrás (expirado)
                .signWith(chave)
                .compact();

        // When
        boolean tokenValido = jwtUtil.validarToken(tokenExpirado);

        // Then
        assertThat(tokenValido).isFalse();
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração configurado")
    void deveRetornarTempoExpiracaoConfigurado() {
        // When
        long expiracao = jwtUtil.obterExpiracaoMs();

        // Then
        assertThat(expiracao).isEqualTo(expiracaoMsTeste);
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair número da conta de token inválido")
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenInvalido() {
        // Given
        String tokenInvalido = "token.invalido.teste";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extrairNumeroConta(tokenInvalido))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair número da conta de token nulo")
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenNulo() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extrairNumeroConta(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CharSequence cannot be null or empty");
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair número da conta de token vazio")
    void deveLancarExcecaoAoExtrairNumeroContaDeTokenVazio() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extrairNumeroConta(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CharSequence cannot be null or empty");
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para contas diferentes")
    void deveGerarTokensDiferentesParaContasDiferentes() {
        // Given
        String numeroConta1 = "12345678";
        String numeroConta2 = "87654321";

        // When
        String token1 = jwtUtil.gerarToken(numeroConta1, "João Silva", "ROLE_USER");
        String token2 = jwtUtil.gerarToken(numeroConta2, "Maria Santos", "ROLE_ADMIN");

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extrairNumeroConta(token1)).isEqualTo(numeroConta1);
        assertThat(jwtUtil.extrairNumeroConta(token2)).isEqualTo(numeroConta2);
    }

    @Test
    @DisplayName("Deve gerar token com data de expiração correta")
    void deveGerarTokenComDataExpiracaoCorreta() {
        // Given
        long tempoAntes = System.currentTimeMillis();

        // When
        String token = jwtUtil.gerarToken(numeroConta, nomeCompleto, role);

        // Then
        SecretKey chave = Keys.hmacShaKeyFor(segredoTeste.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long tempoExpiracao = claims.getExpiration().getTime();
        long tempoEsperado = tempoAntes + expiracaoMsTeste;

        // Permite uma margem de erro de 1 segundo
        assertThat(tempoExpiracao).isBetween(tempoEsperado - 1000, tempoEsperado + 1000);
    }

    @Test
    @DisplayName("Deve gerar token com claims customizadas corretas")
    void deveGerarTokenComClaimsCustomizadasCorretas() {
        // Given
        String nomeCompleto = "Ana Paula Santos";
        String role = "ROLE_ADMIN";

        // When
        String token = jwtUtil.gerarToken(numeroConta, nomeCompleto, role);

        // Then
        SecretKey chave = Keys.hmacShaKeyFor(segredoTeste.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("nomeCompleto", String.class)).isEqualTo(nomeCompleto);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
    }
}