package com.iotroom.auth.controller;

import com.iotroom.auth.dto.AuthResponseDTO;
import com.iotroom.auth.dto.LoginDTO;
import com.iotroom.auth.dto.RefreshTokenDTO;
import com.iotroom.auth.dto.RegisterDTO;
import com.iotroom.auth.dto.UserMeDTO;
import com.iotroom.auth.model.Utilizador;
import com.iotroom.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserMeDTO> register(
            @Valid @RequestBody RegisterDTO dto,
            HttpServletRequest request
    ) {
        UserMeDTO response = authService.registar(
                dto,
                obterIp(request),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request
    ) {
        AuthResponseDTO response = authService.login(
                dto,
                obterIp(request),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenDTO dto,
            HttpServletRequest request
    ) {
        AuthResponseDTO response = authService.refresh(
                dto,
                obterIp(request),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenDTO dto,
            HttpServletRequest request
    ) {
        authService.logout(
                dto,
                obterIp(request),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            Authentication authentication,
            HttpServletRequest request
    ) {
        Utilizador utilizador = (Utilizador) authentication.getPrincipal();

        authService.logoutAll(
                utilizador,
                obterIp(request),
                request.getHeader("User-Agent")
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeDTO> me(Authentication authentication) {
        Utilizador utilizador = (Utilizador) authentication.getPrincipal();
        return ResponseEntity.ok(authService.me(utilizador));
    }

    private String obterIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}