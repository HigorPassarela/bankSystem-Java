package br.com.banksystem.notificacoes.config;
import br.com.banksystem.notificacoes.security.JwtFiltroAutenticacao;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; import java.util.List;
@Configuration @EnableWebSecurity
public class SecurityConfig {
    private final JwtFiltroAutenticacao jwtFiltroAutenticacao;
    public SecurityConfig(JwtFiltroAutenticacao jwtFiltroAutenticacao) { this.jwtFiltroAutenticacao = jwtFiltroAutenticacao; }
    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(configuracaoCors()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/notificacoes/sse","/swagger-ui.html","/swagger-ui/**","/api-docs/**","/actuator/health").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFiltroAutenticacao, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public CorsConfigurationSource configuracaoCors() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:5173","http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*")); cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", cfg); return fonte;
    }
}
