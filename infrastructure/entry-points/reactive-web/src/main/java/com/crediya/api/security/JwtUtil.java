package com.crediya.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMillis;

    public JwtUtil(Environment env) {
        String secret = env.getProperty("jwt.secret", "defaultSecretKey");
        this.expirationMillis = Long.parseLong(env.getProperty("jwt.expiration-ms", "3600000"));
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String subject, String role, List<String> permissions) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setClaims(Map.of(
                        "permissions", permissions,
                        "role", role
                ))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }


    @SuppressWarnings("unchecked")
    public String getRole(String token) {
        return getClaims(token).get("role").toString();
    }


}
