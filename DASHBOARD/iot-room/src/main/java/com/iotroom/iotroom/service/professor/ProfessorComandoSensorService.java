package com.iotroom.iotroom.service.professor;

import com.iotroom.iotroom.dto.professor.SensorModoFormDTO;
import com.iotroom.iotroom.model.*;
import com.iotroom.iotroom.mqtt.MqttCommandPublisher;
import com.iotroom.iotroom.repository.*;
import com.iotroom.iotroom.repository.sensor.ComandoSensorRepository;
import com.iotroom.iotroom.repository.sensor.ConfiguracaoModoSensorRepository;
import com.iotroom.iotroom.repository.sensor.PedidoConfiguracaoSensorRepository;
import com.iotroom.iotroom.repository.sensor.SensorRepository;

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
        return listarSensores(professorId, false);
    }

    public List<Sensor> listarSensores(Long utilizadorId, boolean admin) {
        if (admin) {
            return sensorRepository.findAll();
        }

        return sensorRepository.findSensoresDoProfessor(utilizadorId);
    }

    public Sensor obterSensor(Long sensorId, Long professorId) {
        return obterSensor(sensorId, professorId, false);
    }

    public Sensor obterSensor(Long sensorId, Long utilizadorId, boolean admin) {
        if (!admin) {
            garantirSensorDoProfessor(sensorId, utilizadorId);
        }

        return sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sensor não encontrado."
                ));
    }

    public ConfiguracaoModoSensor obterOuCriarConfiguracao(Long sensorId, Long professorId) {
        return obterOuCriarConfiguracao(sensorId, professorId, false);
    }

    public ConfiguracaoModoSensor obterOuCriarConfiguracao(
            Long sensorId,
            Long utilizadorId,
            boolean admin
    ) {
        Sensor sensor = obterSensor(sensorId, utilizadorId, admin);

        return configuracaoRepository.findBySensorId(sensorId)
                .orElseGet(() -> criarConfiguracaoDefault(sensor));
    }

    public SensorModoFormDTO criarFormModo(Long sensorId, Long professorId) {
        return criarFormModo(sensorId, professorId, false);
    }

    public SensorModoFormDTO criarFormModo(Long sensorId, Long utilizadorId, boolean admin) {
        ConfiguracaoModoSensor config = obterOuCriarConfiguracao(sensorId, utilizadorId, admin);

        return new SensorModoFormDTO(
                config.getIntervaloRapidoMs(),
                config.getIntervaloEstavelMs(),
                config.getDuracaoModoRapidoMs(),
                config.getDeltaSignificativo()
        );
    }

    public List<ComandoSensor> listarComandosRecentes(Long sensorId, Long professorId) {
        return listarComandosRecentes(sensorId, professorId, false);
    }

    public List<ComandoSensor> listarComandosRecentes(
            Long sensorId,
            Long utilizadorId,
            boolean admin
    ) {
        if (!admin) {
            garantirSensorDoProfessor(sensorId, utilizadorId);
        }

        return comandoSensorRepository.findTop10BySensorIdOrderByCriadoEmDesc(sensorId);
    }

    public List<PedidoConfiguracaoSensor> listarPedidosDoSensor(Long sensorId, Long professorId) {
        return listarPedidosDoSensor(sensorId, professorId, false);
    }

    public List<PedidoConfiguracaoSensor> listarPedidosDoSensor(
            Long sensorId,
            Long utilizadorId,
            boolean admin
    ) {
        if (!admin) {
            garantirSensorDoProfessor(sensorId, utilizadorId);
        }

        return pedidoRepository.findTop10BySensorIdOrderByCriadoEmDesc(sensorId);
    }

    public List<PedidoConfiguracaoSensor> listarPedidosPendentes(Long professorId) {
        return listarPedidosPendentes(professorId, false);
    }

    public List<PedidoConfiguracaoSensor> listarPedidosPendentes(Long utilizadorId, boolean admin) {
        List<PedidoConfiguracaoSensor> pedidos =
                pedidoRepository.findByEstadoOrderByCriadoEmDesc("PENDENTE");

        if (admin) {
            return pedidos;
        }

        return pedidos.stream()
                .filter(pedido -> sensorRepository.countSensorDoProfessor(
                        pedido.getSensorId(),
                        utilizadorId
                ) > 0)
                .toList();
    }

    @Transactional
    public void enviarFatorCalibracao(Long sensorId, Long professorId, BigDecimal fator) {
        enviarFatorCalibracao(sensorId, professorId, false, fator);
    }

    @Transactional
    public void enviarFatorCalibracao(
            Long sensorId,
            Long utilizadorId,
            boolean admin,
            BigDecimal fator
    ) {
        Sensor sensor = obterSensor(sensorId, utilizadorId, admin);

        if (fator == null || fator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fator de calibração inválido.");
        }

        sensor.setFatorCalibracao(fator);
        sensorRepository.save(sensor);

        String comando = "SET_FACTOR:" + sensor.getTipo().toUpperCase() + ":" + fator;

        enviarComando(utilizadorId, sensor, comando);
    }

    @Transactional
    public void enviarOffsetPh(Long sensorId, Long professorId, BigDecimal offset) {
        enviarOffsetPh(sensorId, professorId, false, offset);
    }

    @Transactional
    public void enviarOffsetPh(
            Long sensorId,
            Long utilizadorId,
            boolean admin,
            BigDecimal offset
    ) {
        Sensor sensor = obterSensor(sensorId, utilizadorId, admin);

        if (!"PH".equalsIgnoreCase(sensor.getTipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offset só se aplica ao sensor de pH.");
        }

        if (offset == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offset inválido.");
        }

        sensor.setOffsetCalibracao(offset);
        sensorRepository.save(sensor);

        String comando = "SET_OFFSET:PH:" + offset;

        enviarComando(utilizadorId, sensor, comando);
    }

    @Transactional
    public void ligarSensor(Long sensorId, Long professorId) {
        ligarSensor(sensorId, professorId, false);
    }

    @Transactional
    public void ligarSensor(Long sensorId, Long utilizadorId, boolean admin) {
        alterarEstadoRemoto(sensorId, utilizadorId, admin, true);
    }

    @Transactional
    public void desligarSensor(Long sensorId, Long professorId) {
        desligarSensor(sensorId, professorId, false);
    }

    @Transactional
    public void desligarSensor(Long sensorId, Long utilizadorId, boolean admin) {
        alterarEstadoRemoto(sensorId, utilizadorId, admin, false);
    }

    @Transactional
    public void guardarConfiguracaoModo(
            Long sensorId,
            Long professorId,
            SensorModoFormDTO form
    ) {
        guardarConfiguracaoModo(sensorId, professorId, false, form);
    }

    @Transactional
    public void guardarConfiguracaoModo(
            Long sensorId,
            Long utilizadorId,
            boolean admin,
            SensorModoFormDTO form
    ) {
        Sensor sensor = obterSensor(sensorId, utilizadorId, admin);

        validarConfiguracaoModo(form);

        ConfiguracaoModoSensor config = obterOuCriarConfiguracao(sensorId, utilizadorId, admin);

        config.setIntervaloRapidoMs(form.getIntervaloRapidoMs());
        config.setIntervaloEstavelMs(form.getIntervaloEstavelMs());
        config.setDuracaoModoRapidoMs(form.getDuracaoModoRapidoMs());
        config.setDeltaSignificativo(form.getDeltaSignificativo());
        config.setAtualizadoPor(utilizadorId);
        config.setAtualizadaEm(LocalDateTime.now());

        configuracaoRepository.save(config);

        String comando = criarComandoModo(sensor, config);

        enviarComando(utilizadorId, sensor, comando);
    }

    @Transactional
    public void aprovarPedido(Long pedidoId, Long professorId, String respostaProfessor) {
        aprovarPedido(pedidoId, professorId, false, respostaProfessor);
    }

    @Transactional
    public void aprovarPedido(
            Long pedidoId,
            Long utilizadorId,
            boolean admin,
            String respostaProfessor
    ) {
        PedidoConfiguracaoSensor pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido não encontrado."
                ));

        Sensor sensor = obterSensor(pedido.getSensorId(), utilizadorId, admin);

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

        ConfiguracaoModoSensor config = obterOuCriarConfiguracao(sensor.getId(), utilizadorId, admin);
        config.setIntervaloRapidoMs(form.getIntervaloRapidoMs());
        config.setIntervaloEstavelMs(form.getIntervaloEstavelMs());
        config.setDuracaoModoRapidoMs(form.getDuracaoModoRapidoMs());
        config.setDeltaSignificativo(form.getDeltaSignificativo());
        config.setAtualizadoPor(utilizadorId);
        config.setAtualizadaEm(LocalDateTime.now());

        configuracaoRepository.save(config);

        String comando = criarComandoModo(sensor, config);
        ComandoSensor comandoSensor = enviarComando(utilizadorId, sensor, comando);

        pedido.setEstado("APLICADO");
        pedido.setAnalisadoPor(utilizadorId);
        pedido.setRespostaProfessor(normalizarTexto(respostaProfessor));
        pedido.setAnalisadoEm(LocalDateTime.now());
        pedido.setAplicadoEm(LocalDateTime.now());
        pedido.setComandoId(comandoSensor.getId());

        pedidoRepository.save(pedido);
    }

    @Transactional
    public void rejeitarPedido(Long pedidoId, Long professorId, String respostaProfessor) {
        rejeitarPedido(pedidoId, professorId, false, respostaProfessor);
    }

    @Transactional
    public void rejeitarPedido(
            Long pedidoId,
            Long utilizadorId,
            boolean admin,
            String respostaProfessor
    ) {
        PedidoConfiguracaoSensor pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido não encontrado."
                ));

        if (!admin) {
            garantirSensorDoProfessor(pedido.getSensorId(), utilizadorId);
        }

        if (!"PENDENTE".equals(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este pedido já foi analisado.");
        }

        pedido.setEstado("REJEITADO");
        pedido.setAnalisadoPor(utilizadorId);
        pedido.setRespostaProfessor(normalizarTexto(respostaProfessor));
        pedido.setAnalisadoEm(LocalDateTime.now());

        pedidoRepository.save(pedido);
    }

    private void alterarEstadoRemoto(
            Long sensorId,
            Long utilizadorId,
            boolean admin,
            boolean ligado
    ) {
        Sensor sensor = obterSensor(sensorId, utilizadorId, admin);

        sensor.setRemotoAtivo(ligado);
        sensorRepository.save(sensor);

        String comando = "SENSOR:" + sensor.getTipo().toUpperCase() + ":" + (ligado ? "ON" : "OFF");

        enviarComando(utilizadorId, sensor, comando);
    }

    private ComandoSensor enviarComando(Long utilizadorId, Sensor sensor, String comando) {
        String deviceId = sensor.getEstacao().getDeviceId();

        ComandoSensor comandoSensor = new ComandoSensor();
        comandoSensor.setProfessorId(utilizadorId);
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