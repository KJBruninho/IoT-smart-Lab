package com.iotroom.auth.service;

import com.iotroom.auth.model.RefreshToken;
import com.iotroom.auth.model.Utilizador;
import com.iotroom.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityHashService securityHashService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            SecurityHashService securityHashService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityHashService = securityHashService;
    }

    @Transactional
    public RefreshTokenCriado criarRefreshToken(
            Utilizador utilizador,
            String appClient,
            String deviceId,
            String ip,
            String userAgent
    ) {
        String tokenPlano = gerarTokenSeguro();
        String tokenHash = securityHashService.sha256(tokenPlano);
        String deviceIdHash = securityHashService.sha256(deviceId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUtilizador(utilizador);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setDeviceIdHash(deviceIdHash);
        refreshToken.setAppClient(normalizarAppClient(appClient));
        refreshToken.setIp(ip);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setExpiraEm(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000));

        RefreshToken guardado = refreshTokenRepository.save(refreshToken);

        return new RefreshTokenCriado(tokenPlano, guardado);
    }

    @Transactional
    public RefreshTokenCriado renovarRefreshToken(
            String refreshTokenPlano,
            String deviceId,
            String appClient,
            String ip,
            String userAgent
    ) {
        RefreshToken atual = validarRefreshToken(refreshTokenPlano, deviceId);

        atual.setRevogadoEm(LocalDateTime.now());
        refreshTokenRepository.save(atual);

        String novoTokenPlano = gerarTokenSeguro();

        RefreshToken novo = new RefreshToken();
        novo.setUtilizador(atual.getUtilizador());
        novo.setTokenHash(securityHashService.sha256(novoTokenPlano));
        novo.setDeviceIdHash(atual.getDeviceIdHash());
        novo.setAppClient(
                appClient == null || appClient.isBlank()
                        ? atual.getAppClient()
                        : normalizarAppClient(appClient)
        );
        novo.setIp(ip);
        novo.setUserAgent(userAgent);
        novo.setExpiraEm(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000));

        RefreshToken guardado = refreshTokenRepository.save(novo);

        return new RefreshTokenCriado(novoTokenPlano, guardado);
    }

    @Transactional
    public RefreshToken validarRefreshToken(String refreshTokenPlano, String deviceId) {
        String tokenHash = securityHashService.sha256(refreshTokenPlano);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (!refreshToken.estaValido()) {
            throw new IllegalArgumentException("Refresh token expirado ou revogado");
        }

        String deviceHashRecebido = securityHashService.sha256(deviceId);

        if (refreshToken.getDeviceIdHash() != null
                && deviceHashRecebido != null
                && !refreshToken.getDeviceIdHash().equals(deviceHashRecebido)) {
            throw new IllegalArgumentException("Refresh token não pertence a este dispositivo");
        }

        refreshToken.setUltimoUsoEm(LocalDateTime.now());
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Utilizador revogarRefreshToken(String refreshTokenPlano) {
        String tokenHash = securityHashService.sha256(refreshTokenPlano);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (refreshToken.getRevogadoEm() == null) {
            refreshToken.setRevogadoEm(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
        }

        return refreshToken.getUtilizador();
    }

    @Transactional
    public void revogarTodosDoUtilizador(Long utilizadorId) {
        List<RefreshToken> tokens =
                refreshTokenRepository.findByUtilizadorIdAndRevogadoEmIsNull(utilizadorId);

        LocalDateTime agora = LocalDateTime.now();

        for (RefreshToken token : tokens) {
            token.setRevogadoEm(agora);
        }

        refreshTokenRepository.saveAll(tokens);
    }

    @Transactional
    public void revogarTokensDoDispositivo(Long utilizadorId, String deviceIdHash) {
        List<RefreshToken> tokens =
                refreshTokenRepository.findByUtilizadorIdAndDeviceIdHashAndRevogadoEmIsNull(
                        utilizadorId,
                        deviceIdHash
                );

        LocalDateTime agora = LocalDateTime.now();

        for (RefreshToken token : tokens) {
            token.setRevogadoEm(agora);
        }

        refreshTokenRepository.saveAll(tokens);
    }

    private String gerarTokenSeguro() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizarAppClient(String appClient) {
        if (appClient == null || appClient.isBlank()) {
            return "WEB";
        }

        return appClient.trim().toUpperCase();
    }

    public static class RefreshTokenCriado {

        private final String tokenPlano;
        private final RefreshToken refreshToken;

        public RefreshTokenCriado(String tokenPlano, RefreshToken refreshToken) {
            this.tokenPlano = tokenPlano;
            this.refreshToken = refreshToken;
        }

        public String getTokenPlano() {
            return tokenPlano;
        }

        public RefreshToken getRefreshToken() {
            return refreshToken;
        }
    }
}