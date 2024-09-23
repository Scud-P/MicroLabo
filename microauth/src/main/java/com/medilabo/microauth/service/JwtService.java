package com.medilabo.microauth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code JwtService} class provides functionality for generating and validating
 * JSON Web Tokens (JWTs) used for user authentication.
 * This service uses a secret key stored in environment variables for signing and
 * verifying the tokens. The generated tokens can include additional claims as needed.
 */
@Service
public class JwtService {

    /**
     * The secret key used for signing and validating JWT tokens.
     * This key is retrieved from environment variables.
     */
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


    /**
     * Validates the provided JWT token.
     * This method checks the validity of the token by parsing it with the signing key.
     * If the token is invalid or expired, an exception will be thrown.
     *
     * @param token the JWT token to validate
     */
    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    /**
     * Generates a JWT token for the specified username.
     * This method creates a token with the provided username as the subject and
     * an empty claims map. The token is signed with the secret key.
     *
     * @param userName the username for which the token will be generated
     * @return a JWT token as a {@link String}
     */
    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName);
    }

    /**
     * Creates a JWT token with the specified claims and username.
     * This method builds the JWT token using the provided claims and subject,
     * sets the issued date, and specifies the expiration time (30 minutes).
     *
     * @param claims   a map of claims to include in the token
     * @param userName the username for the token's subject
     * @return a compact, signed JWT token as a {@link String}
     */
    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // 30 minutes, le temps d'une consultation
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }
    /**
     * Retrieves the signing key used for JWT token signing.
     * This method decodes the secret key from base64 and returns it as a {@link Key} object.
     *
     * @return the signing key used for JWT signing
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
