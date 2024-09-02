package com.medilabo.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    //TODO add "/eureka" to the list of open api endpoints in case we end up using it
    //TODO ALSO MAYBE ADD LOGIN ENDPOINT TO NON PROTECTED
    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/token",
            "/api-docs",
            "/api/login"
            );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
