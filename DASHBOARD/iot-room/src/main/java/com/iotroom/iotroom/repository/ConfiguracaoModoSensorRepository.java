package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.ConfiguracaoModoSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoModoSensorRepository extends JpaRepository<ConfiguracaoModoSensor, Long> {

    Optional<ConfiguracaoModoSensor> findBySensorId(Long sensorId);
}