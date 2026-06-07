package com.estimplytics.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration, @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes when decoded");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null);
    }

    public String generateToken(UserDetails userDetails, String name) {
        HashMap<String, Object> claims = new HashMap<>();
        if (name != null && !name.isBlank()) {
            claims.put("name", name.trim());
        }
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(key)
            .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    public long getAccessTokenSeconds() {
        return expiration / 1000;
    }
}