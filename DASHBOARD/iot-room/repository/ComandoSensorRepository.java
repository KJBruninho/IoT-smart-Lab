package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.ComandoSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComandoSensorRepository extends JpaRepository<ComandoSensor, Long> {

    List<ComandoSensor> findTop10BySensorIdOrderByCriadoEmDesc(Long sensorId);
}