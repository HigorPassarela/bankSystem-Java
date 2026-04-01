package br.com.banksystem.extratos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração OpenAPI com suporte a JWT Bearer Token no Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiPersonalizado() {
        SecurityScheme esquemaJwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Insira o token JWT obtido no endpoint /api/contas/login");

        SecurityRequirement requisito = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("BankSystem — Serviço de Extratos")
                        .description("Consulta de extratos e geração de PDF")
                        .version("1.0.0"))
                .addSecurityItem(requisito)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", esquemaJwt));
    }
}
