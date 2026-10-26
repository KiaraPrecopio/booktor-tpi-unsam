package com.mdd.booktor.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;


@Configuration 
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                        .title("Booktor API")
                        .version("0.0.1")
                        .description("API de la plataforma de agendamiento de tutorías académicas con enfoque DUA.");
        return new OpenAPI().info(info);
    }
}