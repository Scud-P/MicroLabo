package com.medilabo.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * The {@code RouteValidator} class is responsible for validating routes
 * to determine if they are secured or open. It contains a list of endpoints
 * that are accessible without authentication.
 */
@Component
public class RouteValidator {

    /**
     * A list of open API endpoints that can be accessed without
     * authentication. These endpoints are generally used for
     * registration, login, error handling, and API documentation.
     */
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

    /**
     * A predicate that checks if a given HTTP request is secured.
     * It returns {@code true} if the request does not match any
     * of the open API endpoints, indicating that authentication is
     * required to access the resource.
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
