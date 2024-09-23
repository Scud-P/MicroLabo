package com.medilabo.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;

/**
 * The {@code JwtUtil} class provides utility methods for working with JSON Web Tokens (JWT).
 * This service is responsible for validating JWT tokens using a secret key. It provides
 * methods to validate the integrity of a token by checking its signature and claims.
 */
@Service
public class JwtUtil {

    /**
     * The secret key used for signing and validating JWT tokens. This key should be kept
     * secure and should not be exposed in a production environment.
     */
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


    /**
     * Validates the provided JWT token by parsing it and verifying its signature.
     *
     * @param token the JWT token to validate
     */
    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    /**
     * Retrieves the signing key used for validating JWT tokens.
     *
     * @return the signing key
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
