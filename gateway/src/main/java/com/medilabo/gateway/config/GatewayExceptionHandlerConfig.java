package com.medilabo.gateway.config;

import com.medilabo.gateway.exception.UnauthorizedAccessException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayExceptionHandlerConfig {

    @Bean
    public ErrorWebExceptionHandler globalExceptionHandler() {
        return (exchange, ex) -> {
            if (ex instanceof UnauthorizedAccessException) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            // Handle other exceptions
            return Mono.error(ex);
        };
    }
}
