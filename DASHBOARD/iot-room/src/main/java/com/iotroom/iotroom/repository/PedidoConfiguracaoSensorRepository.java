package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.PedidoConfiguracaoSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoConfiguracaoSensorRepository extends JpaRepository<PedidoConfiguracaoSensor, Long> {

    List<PedidoConfiguracaoSensor> findByEstadoOrderByCriadoEmDesc(String estado);

    List<PedidoConfiguracaoSensor> findTop10BySensorIdOrderByCriadoEmDesc(Long sensorId);
}