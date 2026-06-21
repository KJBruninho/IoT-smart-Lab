package com.iotroom.auth.service;

import com.iotroom.auth.dto.DeviceResponseDTO;
import com.iotroom.auth.model.DispositivoConfiavel;
import com.iotroom.auth.model.Utilizador;
import com.iotroom.auth.repository.DispositivoConfiavelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {

    private final DispositivoConfiavelRepository dispositivoRepository;
    private final SecurityHashService securityHashService;
    private final RefreshTokenService refreshTokenService;

    public DeviceService(
            DispositivoConfiavelRepository dispositivoRepository,
            SecurityHashService securityHashService,
            RefreshTokenService refreshTokenService
    ) {
        this.dispositivoRepository = dispositivoRepository;
        this.securityHashService = securityHashService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void registarOuAtualizarDispositivo(
            Utilizador utilizador,
            String deviceId,
            String deviceName,
            String platform,
            String appClient,
            Boolean biometriaAtiva
    ) {
        if (deviceId == null || deviceId.isBlank()) {
            return;
        }

        String deviceIdHash = securityHashService.sha256(deviceId);
        String appClientNormalizado = normalizarAppClient(appClient);

        DispositivoConfiavel dispositivo = dispositivoRepository
                .findByUtilizadorAndDeviceIdHashAndAppClient(
                        utilizador,
                        deviceIdHash,
                        appClientNormalizado
                )
                .orElseGet(DispositivoConfiavel::new);

        dispositivo.setUtilizador(utilizador);
        dispositivo.setDeviceIdHash(deviceIdHash);
        dispositivo.setNomeDispositivo(deviceName);
        dispositivo.setPlataforma(platform);
        dispositivo.setAppClient(appClientNormalizado);
        dispositivo.setAtivo(true);
        dispositivo.setUltimoAcessoEm(LocalDateTime.now());

        if (biometriaAtiva != null) {
            dispositivo.setBiometriaAtiva(biometriaAtiva);
        }

        dispositivoRepository.save(dispositivo);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponseDTO> listarDispositivos(Long utilizadorId) {
        return dispositivoRepository
                .findByUtilizadorIdOrderByUltimoAcessoEmDesc(utilizadorId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void desativarDispositivo(Utilizador utilizador, Long dispositivoId) {
        DispositivoConfiavel dispositivo = dispositivoRepository.findById(dispositivoId)
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo não encontrado"));

        if (!dispositivo.getUtilizador().getId().equals(utilizador.getId())) {
            throw new IllegalArgumentException("Dispositivo não pertence ao utilizador autenticado");
        }

        dispositivo.setAtivo(false);
        dispositivoRepository.save(dispositivo);

        refreshTokenService.revogarTokensDoDispositivo(
                utilizador.getId(),
                dispositivo.getDeviceIdHash()
        );
    }

    private DeviceResponseDTO toDTO(DispositivoConfiavel dispositivo) {
        return new DeviceResponseDTO(
                dispositivo.getId(),
                dispositivo.getNomeDispositivo(),
                dispositivo.getPlataforma(),
                dispositivo.getAppClient(),
                dispositivo.getBiometriaAtiva(),
                dispositivo.getAtivo(),
                dispositivo.getCriadoEm(),
                dispositivo.getUltimoAcessoEm()
        );
    }

    private String normalizarAppClient(String appClient) {
        if (appClient == null || appClient.isBlank()) {
            return "WEB";
        }

        return appClient.trim().toUpperCase();
    }
}