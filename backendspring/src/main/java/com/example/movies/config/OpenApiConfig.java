package com.example.movies.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI cinemaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RIS Movies API")
                        .description("REST API za rezervaciju filmova, ocene, komentare i watchlist")
                        .version("v1.0"))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH,
                                new SecurityScheme()
                                        .name(BASIC_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
