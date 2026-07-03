package com.example.movies.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // Naziv security šeme koju cemo referencirati u SecurityRequirement-u
    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI cinemaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RIS Movies API")
                        .description("REST API za rezervaciju filmova, ocjene, komentare i watchlist")
                        .version("v1.0"))
                // Registrujemo Basic Auth security šemu (backend koristi Spring Security httpBasic)
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH,
                                new SecurityScheme()
                                        .name(BASIC_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                // Primenjujemo je globalno, da "Authorize" dugme u Swagger UI-ju vazi za sve endpointe
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
