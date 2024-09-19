package com.medilabo.gateway.filter;

import com.medilabo.gateway.exception.UnauthorizedAccessException;
import com.medilabo.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

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
                        return redirectToErrorPage(exchange, "Unauthorized access, redirecting to login page");
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
                    return redirectToErrorPage(exchange, "Unauthorized access, redirecting to login page");
                }
            }
            return chain.filter(exchange);
        });
    }

    private Mono<Void> redirectToErrorPage(ServerWebExchange exchange, String errorMessage) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        HttpHeaders headers = exchange.getResponse().getHeaders();
//        this one works but does not load webjars css trying relative paths tomorrow
//        also can I pass the errorMessage somehow, or make dedicated /unauthorized template with login redirect? Looks easier.
//        headers.setLocation(URI.create("http://192.168.0.22:8080/api/error"));
        String redirectUrl = "/api/login?error=" + UriUtils.encode(errorMessage, StandardCharsets.UTF_8);
        headers.setLocation(URI.create(redirectUrl));
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}
