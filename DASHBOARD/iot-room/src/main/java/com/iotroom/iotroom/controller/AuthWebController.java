package com.iotroom.iotroom.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;

@Controller
@RequestMapping("/auth")
public class AuthWebController {

    private final RestTemplate restTemplate;

    @Value("${auth.api.base-url}")
    private String authApiBaseUrl;

    public AuthWebController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String processarLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AuthApiLoginRequest request = new AuthApiLoginRequest(
                    email,
                    password,
                    "WEB",
                    "browser-web",
                    "Browser",
                    "WEB"
            );

            ResponseEntity<AuthApiLoginResponse> authResponse =
                    restTemplate.postForEntity(
                            authApiBaseUrl + "/api/auth/login",
                            request,
                            AuthApiLoginResponse.class
                    );

            AuthApiLoginResponse body = authResponse.getBody();

            if (body == null || body.accessToken() == null || body.refreshToken() == null) {
                redirectAttributes.addFlashAttribute("erro", "Resposta inválida da Auth API.");
                return "redirect:/auth/login";
            }

            adicionarCookie(
                    response,
                    "access_token",
                    body.accessToken(),
                    Duration.ofSeconds(body.expiresIn())
            );

            adicionarCookie(
                    response,
                    "refresh_token",
                    body.refreshToken(),
                    Duration.ofDays(30)
            );


            if ("ADMIN".equalsIgnoreCase(body.role())) {
                return "redirect:/admin";
            }
            if ("PROFESSOR".equalsIgnoreCase(body.role())) {
                return "redirect:/professor";
            }
            if ("ALUNO".equalsIgnoreCase(body.role())) {
                return "redirect:/aluno";
            }

            return "redirect:/professor";

        } catch (HttpClientErrorException.Unauthorized e) {
            redirectAttributes.addFlashAttribute("erro", "Credenciais inválidas.");
            return "redirect:/auth/login";
        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("erro", "Erro de autenticação: " + e.getResponseBodyAsString());
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao contactar Auth API: " + e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterForm());
        }

        return "auth/register";
    }

    @PostMapping("/register")
    public String processarRegister(
            @ModelAttribute("registerDTO") RegisterForm registerForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (!registerForm.getPassword().equals(registerForm.getConfirmarPassword())) {
                redirectAttributes.addFlashAttribute("erro", "As passwords não coincidem.");
                redirectAttributes.addFlashAttribute("registerDTO", registerForm);
                return "redirect:/auth/register";
            }

            AuthApiRegisterRequest request = new AuthApiRegisterRequest(
                    registerForm.getNome(),
                    registerForm.getEmail(),
                    registerForm.getPassword(),
                    "PROFESSOR"
            );

            restTemplate.postForEntity(
                    authApiBaseUrl + "/api/auth/register",
                    request,
                    AuthApiRegisterResponse.class
            );

            redirectAttributes.addFlashAttribute("sucesso", "Conta criada com sucesso. Já podes iniciar sessão.");
            return "redirect:/auth/login";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("erro", e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("registerDTO", registerForm);
            return "redirect:/auth/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao contactar Auth API: " + e.getMessage());
            redirectAttributes.addFlashAttribute("registerDTO", registerForm);
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = obterCookie(request, "access_token");
        String refreshToken = obterCookie(request, "refresh_token");

        tentarRevogarRefreshToken(accessToken, refreshToken);

        limparCookie(response, "access_token");
        limparCookie(response, "refresh_token");

        return "redirect:/auth/login";
    }

    private void tentarRevogarRefreshToken(String accessToken, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (accessToken != null && !accessToken.isBlank()) {
                headers.setBearerAuth(accessToken);
            }

            HttpEntity<RefreshTokenRequest> entity =
                    new HttpEntity<>(new RefreshTokenRequest(refreshToken), headers);

            restTemplate.exchange(
                    authApiBaseUrl + "/api/auth/logout",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
        } catch (Exception ignored) {
            // Logout local deve funcionar mesmo que a Auth API não consiga revogar o token.
        }
    }

    private void adicionarCookie(
            HttpServletResponse response,
            String nome,
            String valor,
            Duration duracao
    ) {
        ResponseCookie cookie = ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(duracao)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void limparCookie(HttpServletResponse response, String nome) {
        ResponseCookie cookie = ResponseCookie.from(nome, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String obterCookie(HttpServletRequest request, String nome) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (nome.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public record AuthApiLoginRequest(
            String email,
            String password,
            String appClient,
            String deviceId,
            String deviceName,
            String platform
    ) {
    }

    public record AuthApiLoginResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            Long id,
            String nome,
            String email,
            String role
    ) {
    }

    public record AuthApiRegisterRequest(
            String nome,
            String email,
            String password,
            String role
    ) {
    }

    public record AuthApiRegisterResponse(
            Long id,
            String nome,
            String email,
            String role
    ) {
    }

    public record RefreshTokenRequest(
            String refreshToken
    ) {
    }

    public static class RegisterForm {

        @NotBlank
        private String nome;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;

        @NotBlank
        private String confirmarPassword;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmarPassword() {
            return confirmarPassword;
        }

        public void setConfirmarPassword(String confirmarPassword) {
            this.confirmarPassword = confirmarPassword;
        }
    }
}