package br.com.banksystem.notificacoes.config;

import br.com.banksystem.notificacoes.security.JwtFiltroAutenticacao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable) // Proxy vai resolver CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/actuator/health").permitAll()
                        .requestMatchers("/api/notificacoes/sse").permitAll() // SSE público (usa token via query param)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFiltroAutenticacao, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}