package br.com.banksystem.transacoes.config;

import br.com.banksystem.transacoes.security.JwtFiltroAutenticacao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFiltroAutenticacao jwtFiltroAutenticacao;

    public SecurityConfig(JwtFiltroAutenticacao jwtFiltroAutenticacao) {
        this.jwtFiltroAutenticacao = jwtFiltroAutenticacao;
    }

    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/api/transacoes/**") // ← MUDANÇA: Aplicar em TODOS os endpoints
                )) // Só aplica security nos endpoints protegidos
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(jwtFiltroAutenticacao, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}