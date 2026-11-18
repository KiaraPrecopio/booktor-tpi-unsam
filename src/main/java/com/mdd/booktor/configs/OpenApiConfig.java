package com.mdd.booktor.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                        .title("Booktor API")
                        .version("0.0.1")
                        .description("API de la plataforma de agendamiento de tutorías académicas con enfoque DUA.");
        // Agrega el botón "Authorize" de Swagger para pegar el token de /api/auth/login.
        SecurityScheme token = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
        return new OpenAPI().info(info)
                .components(new Components().addSecuritySchemes("token", token))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }
}
