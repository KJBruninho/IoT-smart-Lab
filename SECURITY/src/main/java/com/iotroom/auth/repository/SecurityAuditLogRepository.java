package com.iotroom.auth.repository;

import com.iotroom.auth.model.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    List<SecurityAuditLog> findTop100ByOrderByCriadoEmDesc();

    List<SecurityAuditLog> findTop100ByUtilizadorIdOrderByCriadoEmDesc(Long utilizadorId);
}