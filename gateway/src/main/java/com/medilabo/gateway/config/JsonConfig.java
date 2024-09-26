package com.medilabo.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JSON serialization and deserialization settings.
 * This class configures an {@link ObjectMapper} bean to handle Java 8 date and time types.
 */
@Configuration
public class JsonConfig {

    /**
     * Creates and configures an {@link ObjectMapper} bean.
     * This ObjectMapper is configured to use the {@link JavaTimeModule} to support
     * Java 8 time types such as LocalDate, LocalDateTime, etc.
     *
     * @return a configured {@link ObjectMapper} instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
