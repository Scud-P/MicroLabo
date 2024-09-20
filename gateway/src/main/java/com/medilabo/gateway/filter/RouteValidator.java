package com.medilabo.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/api/login",
            "/api/error",
            "/api/logout-success",
            "/auth/token",
            "/auth/validate",
            "/auth/isAuthenticated",
            "/api-docs",
            "/v2/api-docs",
            "/v3/api-docs",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/configuration/ui",
            "/swagger-resources/configuration/security",
            "/webjars/**"
            );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
