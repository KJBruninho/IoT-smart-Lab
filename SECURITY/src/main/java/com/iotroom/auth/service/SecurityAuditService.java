package com.iotroom.auth.service;

import com.iotroom.auth.model.SecurityAuditLog;
import com.iotroom.auth.model.Utilizador;
import com.iotroom.auth.repository.SecurityAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityAuditService {

    private final SecurityAuditLogRepository securityAuditLogRepository;

    public SecurityAuditService(SecurityAuditLogRepository securityAuditLogRepository) {
        this.securityAuditLogRepository = securityAuditLogRepository;
    }

    @Transactional
    public void registar(
            Utilizador utilizador,
            String tipo,
            String detalhe,
            String appClient,
            String ip,
            String userAgent
    ) {
        SecurityAuditLog log = new SecurityAuditLog();
        log.setUtilizador(utilizador);
        log.setTipo(tipo);
        log.setDetalhe(detalhe);
        log.setAppClient(appClient);
        log.setIp(ip);
        log.setUserAgent(userAgent);

        securityAuditLogRepository.save(log);
    }
}