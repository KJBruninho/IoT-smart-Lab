package com.iotroom.auth.service;

import com.iotroom.auth.dto.AuthResponseDTO;
import com.iotroom.auth.dto.LoginDTO;
import com.iotroom.auth.dto.RefreshTokenDTO;
import com.iotroom.auth.dto.RegisterDTO;
import com.iotroom.auth.dto.UserMeDTO;
import com.iotroom.auth.model.RefreshToken;
import com.iotroom.auth.model.Utilizador;
import com.iotroom.auth.repository.UtilizadorRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final DeviceService deviceService;
    private final SecurityAuditService securityAuditService;

    public AuthService(
            UtilizadorRepository utilizadorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            DeviceService deviceService,
            SecurityAuditService securityAuditService
    ) {
        this.utilizadorRepository = utilizadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.deviceService = deviceService;
        this.securityAuditService = securityAuditService;
    }

    @Transactional
    public UserMeDTO registar(RegisterDTO dto, String ip, String userAgent) {
        String email = normalizarEmail(dto.getEmail());

        if (utilizadorRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Já existe um utilizador com este email");
        }

        String role = normalizarRole(dto.getRole());

        if ("ADMIN".equals(role)) {
            throw new IllegalArgumentException("Não é permitido criar administradores por registo público");
        }

        Utilizador utilizador = new Utilizador();
        utilizador.setNome(dto.getNome().trim());
        utilizador.setEmail(email);
        utilizador.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        utilizador.setRole(role);
        utilizador.setAtivo(true);

        Utilizador guardado = utilizadorRepository.save(utilizador);

        securityAuditService.registar(
                guardado,
                "REGISTER_SUCCESS",
                "Utilizador registado",
                "WEB",
                ip,
                userAgent
        );

        return new UserMeDTO(
                guardado.getId(),
                guardado.getNome(),
                guardado.getEmail(),
                guardado.getRole()
        );
    }

    @Transactional
    public AuthResponseDTO login(LoginDTO dto, String ip, String userAgent) {
        String email = normalizarEmail(dto.getEmail());

        Utilizador utilizador = utilizadorRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (Boolean.FALSE.equals(utilizador.getAtivo())) {
            throw new IllegalArgumentException("Utilizador inativo");
        }

        if (!passwordEncoder.matches(dto.getPassword(), utilizador.getPasswordHash())) {
            securityAuditService.registar(
                    utilizador,
                    "LOGIN_FAILED",
                    "Password inválida",
                    dto.getAppClient(),
                    ip,
                    userAgent
            );

            throw new BadCredentialsException("Credenciais inválidas");
        }

        deviceService.registarOuAtualizarDispositivo(
                utilizador,
                dto.getDeviceId(),
                dto.getDeviceName(),
                dto.getPlatform(),
                dto.getAppClient(),
                false
        );

        String accessToken = jwtService.gerarAccessToken(utilizador);

        RefreshTokenService.RefreshTokenCriado refreshCriado =
                refreshTokenService.criarRefreshToken(
                        utilizador,
                        dto.getAppClient(),
                        dto.getDeviceId(),
                        ip,
                        userAgent
                );

        securityAuditService.registar(
                utilizador,
                "LOGIN_SUCCESS",
                "Login efetuado com sucesso",
                dto.getAppClient(),
                ip,
                userAgent
        );

        return new AuthResponseDTO(
                accessToken,
                refreshCriado.getTokenPlano(),
                jwtService.getAccessTokenExpirationSeconds(),
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getEmail(),
                utilizador.getRole()
        );
    }

    @Transactional
    public AuthResponseDTO refresh(RefreshTokenDTO dto, String ip, String userAgent) {
        RefreshTokenService.RefreshTokenCriado refreshCriado =
                refreshTokenService.renovarRefreshToken(
                        dto.getRefreshToken(),
                        dto.getDeviceId(),
                        dto.getAppClient(),
                        ip,
                        userAgent
                );

        RefreshToken refreshToken = refreshCriado.getRefreshToken();
        Utilizador utilizador = refreshToken.getUtilizador();

        if (Boolean.FALSE.equals(utilizador.getAtivo())) {
            throw new IllegalArgumentException("Utilizador inativo");
        }

        String accessToken = jwtService.gerarAccessToken(utilizador);

        securityAuditService.registar(
                utilizador,
                "TOKEN_REFRESH",
                "Access token renovado",
                dto.getAppClient(),
                ip,
                userAgent
        );

        return new AuthResponseDTO(
                accessToken,
                refreshCriado.getTokenPlano(),
                jwtService.getAccessTokenExpirationSeconds(),
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getEmail(),
                utilizador.getRole()
        );
    }

    @Transactional
    public void logout(RefreshTokenDTO dto, String ip, String userAgent) {
        Utilizador utilizador = refreshTokenService.revogarRefreshToken(dto.getRefreshToken());

        securityAuditService.registar(
                utilizador,
                "LOGOUT",
                "Logout efetuado",
                dto.getAppClient(),
                ip,
                userAgent
        );
    }

    @Transactional
    public void logoutAll(Utilizador utilizador, String ip, String userAgent) {
        refreshTokenService.revogarTodosDoUtilizador(utilizador.getId());

        securityAuditService.registar(
                utilizador,
                "LOGOUT_ALL",
                "Todas as sessões foram revogadas",
                null,
                ip,
                userAgent
        );
    }

    public UserMeDTO me(Utilizador utilizador) {
        return new UserMeDTO(
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getEmail(),
                utilizador.getRole()
        );
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizarRole(String role) {
        if (role == null || role.isBlank()) {
            return "PROFESSOR";
        }

        return role.trim().toUpperCase();
    }
}