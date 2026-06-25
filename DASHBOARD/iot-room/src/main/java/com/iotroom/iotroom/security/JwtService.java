package com.iotroom.iotroom.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret deve ter pelo menos 32 caracteres");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean tokenValido(String token) {
        try {
            obterClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims obterClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthenticatedUser obterUtilizadorAutenticado(String token) {
        Claims claims = obterClaims(token);

        Long id = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String nome = claims.get("nome", String.class);
        String role = claims.get("role", String.class);

        return new AuthenticatedUser(id, email, nome, role);
    }
}