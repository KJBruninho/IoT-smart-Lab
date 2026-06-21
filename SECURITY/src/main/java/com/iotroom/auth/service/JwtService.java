package com.iotroom.auth.service;

import com.iotroom.auth.model.Utilizador;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret deve ter pelo menos 32 caracteres");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String gerarAccessToken(Utilizador utilizador) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(utilizador.getId()))
                .claim("email", utilizador.getEmail())
                .claim("nome", utilizador.getNome())
                .claim("role", utilizador.getRole())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getSigningKey())
                .compact();
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

    public Long obterUtilizadorId(String token) {
        String subject = obterClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    public String obterEmail(String token) {
        return obterClaims(token).get("email", String.class);
    }

    public String obterRole(String token) {
        return obterClaims(token).get("role", String.class);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }
}