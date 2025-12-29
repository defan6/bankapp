package com.bankapp.userservice.util;

import com.bankapp.userservice.domain.token.dto.RefreshTokenDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public String generateAccessToken(UUID userId, String username, Set<String> roles) {
        long accessTokenExpirationDateInMillis = getAccessTokenExpirationDateInMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("user_id", userId.toString());
        return createToken(claims, username, accessTokenExpirationDateInMillis);
    }


    public RefreshTokenDetails generateRefreshToken(String username){
        long refreshTokenExpirationDateInMillis = getRefreshTokenExpirationDateInMillis();
        String token = createToken(new HashMap<>(), username, refreshTokenExpirationDateInMillis);
        return new RefreshTokenDetails(token, refreshTokenExpirationDateInMillis);
    }

    private String createToken(Map<String, Object> claims, String username, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .signWith(getKey())
                .compact();
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaims(String token, Function<Claims, T> resolve) {
        return resolve.apply(extractAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaims(token, claims -> claims.getSubject());
    }

    public Boolean isTokenValid(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, claims -> claims.getExpiration());
    }

    public Set<String> extractRoles(String token) {
        return extractClaims(token, claims -> claims.get("roles", Set.class));
    }

    public String extractUserId(String token){
        return extractClaims(token, claims -> claims.get("user_id", String.class));
    }

    private long getRefreshTokenExpirationDateInMillis() {
        return Instant.now()
                .plusMillis(refreshTokenExpiration)
                .toEpochMilli();
    }


    private long getAccessTokenExpirationDateInMillis() {
        return Instant.now()
                .plusMillis(accessTokenExpiration)
                .toEpochMilli();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
