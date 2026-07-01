package com.iotroom.assistant.service;

import com.iotroom.assistant.dto.AuthUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthApiClient {

    private final RestClient restClient;

    public AuthApiClient(@Value("${auth.api.base-url}") String authApiBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authApiBaseUrl)
                .build();
    }

    public AuthUserDTO obterUtilizadorAtual(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        try {
            return restClient.get()
                    .uri("/api/auth/me")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(AuthUserDTO.class);

        } catch (Exception e) {
            return null;
        }
    }
}