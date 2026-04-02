package com.starwars.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Configures the {@link RestClient} used to communicate with the Star Wars API.
 */
@Configuration
public class RestClientConfig {

    @Value("${app.swapi.base-url}")
    private String swapiBaseUrl;

    /**
     * Creates a {@link RestClient} pre-configured with the SWAPI base URL
     * and a default {@code Accept: application/json} header.
     *
     * @return configured RestClient instance
     */
    @Bean
    public RestClient swapiRestClient() {
        return RestClient.builder()
                .baseUrl(swapiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}