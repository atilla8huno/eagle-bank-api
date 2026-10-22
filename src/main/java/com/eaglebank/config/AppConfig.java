package com.eaglebank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Eagle Bank REST API",
                version = "v1.0.0",
                description = "REST API for Eagle Bank conforming to the OpenAPI 3.1 specification with OAuth2 Resource Server JWT security and Optimistic Concurrency Control."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
