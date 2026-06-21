package com.iotroom.auth.repository;

import com.iotroom.auth.model.DispositivoConfiavel;
import com.iotroom.auth.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DispositivoConfiavelRepository extends JpaRepository<DispositivoConfiavel, Long> {

    List<DispositivoConfiavel> findByUtilizadorIdOrderByUltimoAcessoEmDesc(Long utilizadorId);

    Optional<DispositivoConfiavel> findByUtilizadorAndDeviceIdHashAndAppClient(
            Utilizador utilizador,
            String deviceIdHash,
            String appClient
    );
}