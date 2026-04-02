package com.starwars.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI / Swagger UI documentation.
 *
 * <p>All protected endpoints display a padlock icon in Swagger UI.
 * Click "Authorize", paste the JWT token (without the "Bearer " prefix),
 * and subsequent requests will include the correct Authorization header.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Star Wars Integration Platform API",
                version = "1.0.0",
                description = "REST API that integrates with the public Star Wars API (swapi.tech). "
                              + "Exposes paginated and filterable endpoints for People, Films, Starships and Vehicles. "
                              + "All resource endpoints are protected — authenticate first via /api/auth/register or /api/auth/login.",
                contact = @Contact(name = "Star Wars Platform", email = "admin@starwars-platform.dev")
        ),
        servers = @Server(url = "/", description = "Local server")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Paste the JWT token returned by /api/auth/login (without the 'Bearer ' prefix)"
)
public class OpenApiConfig {}