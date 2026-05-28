package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByEstacaoDeviceIdAndTipoAndAtivoTrueAndEstacaoAtivaTrue(String deviceId, String tipo);
}
