package br.com.banksystem.contas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT — intercepta requisições e valida o token Bearer.
 */
@Component
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ContaUserDetailsService userDetailsService;

    public JwtFiltroAutenticacao(JwtUtil jwtUtil, ContaUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecalhoAutorizacao = request.getHeader("Authorization");

        if (cabecalhoAutorizacao != null && cabecalhoAutorizacao.startsWith("Bearer ")) {
            String token = cabecalhoAutorizacao.substring(7);

            if (jwtUtil.validarToken(token)) {
                String numeroConta = jwtUtil.extrairNumeroConta(token);

                if (numeroConta != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(numeroConta);
                    UsernamePasswordAuthenticationToken autenticacao =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
