package com.medilabo.gateway.filter;

import com.medilabo.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * The {@code AuthenticationFilter} class is a Spring Cloud Gateway filter
 * that intercepts requests to validate JWT tokens for secured endpoints.
 * This filter checks if a request is secured based on the configured
 * {@link RouteValidator}. If a request is secured, it attempts to retrieve
 * the JWT token from the Authorization header or from cookies. If the token
 * is missing or invalid, the filter redirects the user to the login page
 * with an appropriate error message.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Constructs a new {@code AuthenticationFilter}.
     */
    public AuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Applies the authentication filter to the given exchange and chain.
     *
     * @param config the configuration for this filter
     * @return a {@link GatewayFilter} that processes the request
     */
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                // Check for the token in the Authorization header first
                String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authorizationHeader == null) {
                    // If not present, check for the token in cookies
                    if (exchange.getRequest().getCookies().containsKey("token")) {
                        authorizationHeader = exchange.getRequest().getCookies().getFirst("token").getValue();
                    } else {
                        return redirectToErrorPage(exchange);
                    }
                }
                // Remove the "Bearer " part if the header is present
                if (authorizationHeader.startsWith("Bearer ")) {
                    authorizationHeader = authorizationHeader.substring(7);
                }

                // Validate the token
                try {
                    jwtUtil.validateToken(authorizationHeader);
                } catch (Exception e) {
                    return redirectToErrorPage(exchange);
                }
            }
            return chain.filter(exchange);
        });
    }

    /**
     * Redirects the user to the login page with an error message.
     *
     * @param exchange the current server web exchange
     * @return a {@link Mono} that completes when the response is set
     */
    private Mono<Void> redirectToErrorPage(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        HttpHeaders headers = exchange.getResponse().getHeaders();
        String redirectUrl = "/api/login?error=" + UriUtils.encode("Unauthorized access, redirecting to login page", StandardCharsets.UTF_8);
        headers.setLocation(URI.create(redirectUrl));
        return exchange.getResponse().setComplete();
    }

    /**
     * Configuration class for the {@code AuthenticationFilter}.
     */
    public static class Config {
    }
}
