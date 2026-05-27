package com.stockflow.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        String bearerScheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("StockFlow API")
                        .version("0.1.0")
                        .description("Spring Boot REST API for products, inventory movements, orders, stock consistency, and reports."))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
                .schemaRequirement(bearerScheme, new SecurityScheme()
                        .name(bearerScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
