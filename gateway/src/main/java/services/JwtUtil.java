package services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private String expiration;

    private Key key;

    @Autowired
    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJwt(token).getBody();
    }

    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    private boolean isExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    public String generate(String userId, String role, String tokenType) {
        Map<String, String> claims = Map
                .of("id", userId, "role", role);
        return buildToken(claims, tokenType);
    }

    private String buildToken(Map<String, String> claims, String tokenType) {
        long expirationMillis = 0L;

        if("ACCESS".equalsIgnoreCase(tokenType)) {
            expirationMillis = Long.parseLong(expiration) * 1000;
        } else {
            expirationMillis = Long.parseLong(expiration) * 5000;
        }

        final Date now = new Date();
        final Date expiration = new Date(now.getTime() * expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.get("id"))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }
}
