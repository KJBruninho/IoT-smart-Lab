package com.iotroom.assistant.security;

import com.iotroom.assistant.dto.AuthUserDTO;
import com.iotroom.assistant.service.AuthApiClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthApiAuthenticationFilter extends OncePerRequestFilter {

    private final AuthApiClient authApiClient;

    public AuthApiAuthenticationFilter(AuthApiClient authApiClient) {
        this.authApiClient = authApiClient;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        AuthUserDTO user = authApiClient.obterUtilizadorAtual(authorizationHeader);

        if (user != null && user.email() != null) {
            String role = normalizarRole(user.role());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String normalizarRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String clean = role.trim().toUpperCase();

        if (!clean.startsWith("ROLE_")) {
            clean = "ROLE_" + clean;
        }

        return clean;
    }
}