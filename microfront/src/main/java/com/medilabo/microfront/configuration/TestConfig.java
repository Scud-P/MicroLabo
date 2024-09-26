package com.medilabo.microfront.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for setting up beans used in the application.
 * This class provides the configuration for creating and customizing a
 * {@link WebClient.Builder} bean, which can be used to make
 * HTTP requests to external services.
 */
@Configuration
public class TestConfig {
    /**
     * Creates and configures a {@link WebClient.Builder} bean.
     * This builder is configured with a base URL pointing to the gateway
     * service (<a href="http://gateway:8080">...</a>) and can be used by other components
     * to make HTTP requests via the gateway.
     *
     * @return a {@link WebClient.Builder} instance with a preconfigured base URL
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .baseUrl("http://gateway:8080");
    }
}
