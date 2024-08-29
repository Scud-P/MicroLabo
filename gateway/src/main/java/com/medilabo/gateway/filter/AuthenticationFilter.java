package com.medilabo.experiment.gateway.filter;

import com.medilabo.experiment.gateway.util.JwtUtil;
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
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    //TODO create custom missing header exception
                    throw new RuntimeException("Missing authorization header");
                }

                String authorizationHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                    // Remove the "Bearer " part of the header, so 6 letters plus the space.
                    authorizationHeader = authorizationHeader.substring(7);
                }
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
