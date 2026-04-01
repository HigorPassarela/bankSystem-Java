package br.com.banksystem.contas.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitário para geração e validação de tokens JWT.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String segredo;

    @Value("${jwt.expiracao}")
    private long expiracaoMs;

    private SecretKey obterChave() {
        return Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String numeroConta, String nomeCompleto, String role) {
        return Jwts.builder()
                .subject(numeroConta)
                .claim("nomeCompleto", nomeCompleto)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracaoMs))
                .signWith(obterChave())
                .compact();
    }

    public String extrairNumeroConta(String token) {
        return parsearToken(token).getPayload().getSubject();
    }

    public boolean validarToken(String token) {
        try {
            parsearToken(token);
            return true;
        } catch (JwtException ex) {
            log.warn("Token JWT inválido: {}", ex.getMessage());
            return false;
        }
    }

    public long obterExpiracaoMs() {
        return expiracaoMs;
    }

    private Jws<Claims> parsearToken(String token) {
        return Jwts.parser()
                .verifyWith(obterChave())
                .build()
                .parseSignedClaims(token);
    }
}
