package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.SensorModoFormDTO;
import com.iotroom.iotroom.model.*;
import com.iotroom.iotroom.mqtt.MqttCommandPublisher;
import com.iotroom.iotroom.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessorComandoSensorService {

    private final SensorRepository sensorRepository;
    private final ConfiguracaoModoSensorRepository configuracaoRepository;
    private final ComandoSensorRepository comandoSensorRepository;
    private final PedidoConfiguracaoSensorRepository pedidoRepository;
    private final MqttCommandPublisher mqttCommandPublisher;

    public ProfessorComandoSensorService(
            SensorRepository sensorRepository,
            ConfiguracaoModoSensorRepository configuracaoRepository,
            ComandoSensorRepository comandoSensorRepository,
            PedidoConfiguracaoSensorRepository pedidoRepository,
            MqttCommandPublisher mqttCommandPublisher
    ) {
        this.sensorRepository = sensorRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.comandoSensorRepository = comandoSensorRepository;
        this.pedidoRepository = pedidoRepository;
        this.mqttCommandPublisher = mqttCommandPublisher;
    }

    public List<Sensor> listarSensores(Long professorId) {
        return sensorRepository.findSensoresDoProfessor(professorId);
    }

    public Sensor obterSensor(Long sensorId, Long professorId) {
        garantirSensorDoProfessor(sensorId, professorId);

        return sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sensor não encontrado."
                ));
    }

    public ConfiguracaoModoSensor obterOuCriarConfiguracao(Long sensorId, Long professorId) {
        Sensor sensor = obterSensor(sensorId, professorId);

        return configuracaoRepository.findBySensorId(sensorId)
                .orElseGet(() -> criarConfiguracaoDefault(sensor));
    }

    public SensorModoFormDTO criarFormModo(Long sensorId, Long professorId) {
        ConfiguracaoModoSensor config = obterOuCriarConfiguracao(sensorId, professorId);

        return new SensorModoFormDTO(
                config.getIntervaloRapidoMs(),
                config.getIntervaloEstavelMs(),
                config.getDuracaoModoRapidoMs(),
                config.getDeltaSignificativo()
        );
    }

    public List<ComandoSensor> listarComandosRecentes(Long sensorId, Long professorId) {
        garantirSensorDoProfessor(sensorId, professorId);

        return comandoSensorRepository.findTop10BySensorIdOrderByCriadoEmDesc(sensorId);
    }

    public List<PedidoConfiguracaoSensor> listarPedidosDoSensor(Long sensorId, Long professorId) {
        garantirSensorDoProfessor(sensorId, professorId);

        return pedidoRepository.findTop10BySensorIdOrderByCriadoEmDesc(sensorId);
    }

    public List<PedidoConfiguracaoSensor> listarPedidosPendentes(Long professorId) {
        List<PedidoConfiguracaoSensor> pedidos = pedidoRepository.findByEstadoOrderByCriadoEmDesc("PENDENTE");

        return pedidos.stream()
                .filter(pedido -> sensorRepository.countSensorDoProfessor(pedido.getSensorId(), professorId) > 0)
                .toList();
    }

    @Transactional
    public void enviarFatorCalibracao(Long sensorId, Long professorId, BigDecimal fator) {
        Sensor sensor = obterSensor(sensorId, professorId);

        if (fator == null || fator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fator de calibração inválido.");
        }

        sensor.setFatorCalibracao(fator);
        sensorRepository.save(sensor);

        String comando = "SET_FACTOR:" + sensor.getTipo().toUpperCase() + ":" + fator;

        enviarComando(professorId, sensor, comando);
    }

    @Transactional
    public void enviarOffsetPh(Long sensorId, Long professorId, BigDecimal offset) {
        Sensor sensor = obterSensor(sensorId, professorId);

        if (!"PH".equalsIgnoreCase(sensor.getTipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offset só se aplica ao sensor de pH.");
        }

        if (offset == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offset inválido.");
        }

        sensor.setOffsetCalibracao(offset);
        sensorRepository.save(sensor);

        String comando = "SET_OFFSET:PH:" + offset;

        enviarComando(professorId, sensor, comando);
    }

    @Transactional
    public void ligarSensor(Long sensorId, Long professorId) {
        alterarEstadoRemoto(sensorId, professorId, true);
    }

    @Transactional
    public void desligarSensor(Long sensorId, Long professorId) {
        alterarEstadoRemoto(sensorId, professorId, false);
    }

    @Transactional
    public void guardarConfiguracaoModo(
            Long sensorId,
            Long professorId,
            SensorModoFormDTO form
    ) {
        Sensor sensor = obterSensor(sensorId, professorId);

        validarConfiguracaoModo(form);

        ConfiguracaoModoSensor config = obterOuCriarConfiguracao(sensorId, professorId);

        config.setIntervaloRapidoMs(form.getIntervaloRapidoMs());
        config.setIntervaloEstavelMs(form.getIntervaloEstavelMs());
        config.setDuracaoModoRapidoMs(form.getDuracaoModoRapidoMs());
        config.setDeltaSignificativo(form.getDeltaSignificativo());
        config.setAtualizadoPor(professorId);
        config.setAtualizadaEm(LocalDateTime.now());

        configuracaoRepository.save(config);

        String comando = criarComandoModo(sensor, config);

        enviarComando(professorId, sensor, comando);
    }

    @Transactional
    public void aprovarPedido(Long pedidoId, Long professorId, String respostaProfessor) {
        PedidoConfiguracaoSensor pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido não encontrado."
                ));

        Sensor sensor = obterSensor(pedido.getSensorId(), professorId);

        if (!"PENDENTE".equals(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este pedido já foi analisado.");
        }

        SensorModoFormDTO form = new SensorModoFormDTO(
                pedido.getIntervaloRapidoMs(),
                pedido.getIntervaloEstavelMs(),
                pedido.getDuracaoModoRapidoMs(),
                pedido.getDeltaSignificativo()
        );

        validarConfiguracaoModo(form);

        ConfiguracaoModoSensor config = obterOuCriarConfiguracao(sensor.getId(), professorId);
        config.setIntervaloRapidoMs(form.getIntervaloRapidoMs());
        config.setIntervaloEstavelMs(form.getIntervaloEstavelMs());
        config.setDuracaoModoRapidoMs(form.getDuracaoModoRapidoMs());
        config.setDeltaSignificativo(form.getDeltaSignificativo());
        config.setAtualizadoPor(professorId);
        config.setAtualizadaEm(LocalDateTime.now());

        configuracaoRepository.save(config);

        String comando = criarComandoModo(sensor, config);
        ComandoSensor comandoSensor = enviarComando(professorId, sensor, comando);

        pedido.setEstado("APLICADO");
        pedido.setAnalisadoPor(professorId);
        pedido.setRespostaProfessor(normalizarTexto(respostaProfessor));
        pedido.setAnalisadoEm(LocalDateTime.now());
        pedido.setAplicadoEm(LocalDateTime.now());
        pedido.setComandoId(comandoSensor.getId());

        pedidoRepository.save(pedido);
    }

    @Transactional
    public void rejeitarPedido(Long pedidoId, Long professorId, String respostaProfessor) {
        PedidoConfiguracaoSensor pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido não encontrado."
                ));

        garantirSensorDoProfessor(pedido.getSensorId(), professorId);

        if (!"PENDENTE".equals(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este pedido já foi analisado.");
        }

        pedido.setEstado("REJEITADO");
        pedido.setAnalisadoPor(professorId);
        pedido.setRespostaProfessor(normalizarTexto(respostaProfessor));
        pedido.setAnalisadoEm(LocalDateTime.now());

        pedidoRepository.save(pedido);
    }

    private void alterarEstadoRemoto(Long sensorId, Long professorId, boolean ligado) {
        Sensor sensor = obterSensor(sensorId, professorId);

        sensor.setRemotoAtivo(ligado);
        sensorRepository.save(sensor);

        String comando = "SENSOR:" + sensor.getTipo().toUpperCase() + ":" + (ligado ? "ON" : "OFF");

        enviarComando(professorId, sensor, comando);
    }

    private ComandoSensor enviarComando(Long professorId, Sensor sensor, String comando) {
        String deviceId = sensor.getEstacao().getDeviceId();

        ComandoSensor comandoSensor = new ComandoSensor();
        comandoSensor.setProfessorId(professorId);
        comandoSensor.setSensorId(sensor.getId());
        comandoSensor.setDeviceId(deviceId);
        comandoSensor.setTipoSensor(sensor.getTipo().toUpperCase());
        comandoSensor.setComando(comando);
        comandoSensor.setEstado("ENVIADO");
        comandoSensor.setCriadoEm(LocalDateTime.now());

        ComandoSensor guardado = comandoSensorRepository.save(comandoSensor);

        mqttCommandPublisher.enviarComando(deviceId, comando);

        return guardado;
    }

    private String criarComandoModo(Sensor sensor, ConfiguracaoModoSensor config) {
        return "SET_MODE:"
                + sensor.getTipo().toUpperCase()
                + ":"
                + config.getIntervaloRapidoMs()
                + ":"
                + config.getIntervaloEstavelMs()
                + ":"
                + config.getDuracaoModoRapidoMs()
                + ":"
                + config.getDeltaSignificativo();
    }

    private void validarConfiguracaoModo(SensorModoFormDTO form) {
        if (form == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configuração inválida.");
        }

        if (form.getIntervaloRapidoMs() == null || form.getIntervaloRapidoMs() < 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O intervalo rápido deve ser pelo menos 500 ms.");
        }

        if (form.getIntervaloEstavelMs() == null || form.getIntervaloEstavelMs() < form.getIntervaloRapidoMs()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O intervalo estável deve ser maior ou igual ao rápido.");
        }

        if (form.getDuracaoModoRapidoMs() == null || form.getDuracaoModoRapidoMs() < form.getIntervaloRapidoMs()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A duração do modo rápido é inválida.");
        }

        if (form.getDeltaSignificativo() == null || form.getDeltaSignificativo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O delta significativo deve ser superior a zero.");
        }
    }

    private ConfiguracaoModoSensor criarConfiguracaoDefault(Sensor sensor) {
        ConfiguracaoModoSensor config = new ConfiguracaoModoSensor();

        config.setSensorId(sensor.getId());
        config.setIntervaloRapidoMs(1000);
        config.setIntervaloEstavelMs(30000);
        config.setDuracaoModoRapidoMs(120000);

        String tipo = sensor.getTipo() != null ? sensor.getTipo().toUpperCase() : "";

        if ("TEMPERATURA".equals(tipo)) {
            config.setDeltaSignificativo(new BigDecimal("0.2000"));
        } else if ("TDS".equals(tipo)) {
            config.setDeltaSignificativo(new BigDecimal("5.0000"));
        } else if ("PH".equals(tipo)) {
            config.setDeltaSignificativo(new BigDecimal("0.1000"));
        } else {
            config.setDeltaSignificativo(new BigDecimal("1.0000"));
        }

        return configuracaoRepository.save(config);
    }

    private void garantirSensorDoProfessor(Long sensorId, Long professorId) {
        if (sensorRepository.countSensorDoProfessor(sensorId, professorId) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Sensor não encontrado para este professor."
            );
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}