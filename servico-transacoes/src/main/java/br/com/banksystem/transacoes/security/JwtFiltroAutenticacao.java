package br.com.banksystem.transacoes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltroAutenticacao extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFiltroAutenticacao(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        System.out.println("\n=== JWT FILTER DEBUG ===");
        System.out.println("🔍 Path: " + req.getRequestURI());
        System.out.println("🔍 Method: " + req.getMethod());
        System.out.println("🔍 Remote Address: " + req.getRemoteAddr());

        String header = req.getHeader("Authorization");
        System.out.println("🔍 Authorization Header: " + (header != null ? "Bearer " + header.substring(7, Math.min(27, header.length())) + "..." : "❌ NULL"));

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("🔑 Validando token...");

            if (jwtUtil.validarToken(token)) {
                String numeroConta = jwtUtil.extrairNumeroConta(token);
                System.out.println("✅ Token válido para conta: " + numeroConta);

                if (numeroConta != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            numeroConta, null, List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("✅ SecurityContext configurado com sucesso");
                } else {
                    System.out.println("⚠️ SecurityContext já configurado ou numeroConta null");
                }
            } else {
                System.out.println("❌ Token inválido ou expirado!");
            }
        } else {
            System.out.println("❌ Header Authorization não encontrado ou formato inválido");
        }

        System.out.println("🚀 Continuando com a requisição...");
        System.out.println("=== FIM JWT FILTER ===\n");

        chain.doFilter(req, res);
    }
}
