package com.medilabo.gateway.filter;

import com.medilabo.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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
                        // Both header and cookie are missing, throw exception
                        throw new RuntimeException("Missing authorization header or cookie");
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
                    throw new RuntimeException("Unauthorized access");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
