package com.iotroom.iotroom.service.sensor;

import com.iotroom.iotroom.dto.professor.SensorDisponivelDTO;
import com.iotroom.iotroom.dto.sensor.PedidoConfiguracaoSensorDTO;
import com.iotroom.iotroom.dto.sensor.PedidoConfiguracaoSensorRequest;
import com.iotroom.iotroom.dto.sensor.PedidoConfiguracaoSensorResponse;
import com.iotroom.iotroom.model.PedidoConfiguracaoSensor;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.repository.sensor.PedidoConfiguracaoSensorRepository;
import com.iotroom.iotroom.repository.sensor.SensorRepository;
import com.iotroom.iotroom.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoConfiguracaoSensorService {

    private final SensorRepository sensorRepository;
    private final PedidoConfiguracaoSensorRepository pedidoRepository;

    public PedidoConfiguracaoSensorService(
            SensorRepository sensorRepository,
            PedidoConfiguracaoSensorRepository pedidoRepository
    ) {
        this.sensorRepository = sensorRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<SensorDisponivelDTO> listarSensoresDisponiveis(AuthenticatedUser user) {
        List<Sensor> sensores;

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            sensores = pedidoRepository.findSensoresAtivosComEstacao();
        } else {
            sensores = pedidoRepository.findSensoresDisponiveisParaUtilizador(user.getId());
        }

        return sensores.stream()
                .map(this::toSensorDisponivelDTO)
                .toList();
    }

    @Transactional
    public PedidoConfiguracaoSensorResponse criarPedido(
            AuthenticatedUser user,
            PedidoConfiguracaoSensorRequest request
    ) {
        validarRequest(request);

        Sensor sensor = sensorRepository.findById(request.sensorId())
                .orElseThrow(() -> new IllegalArgumentException("Sensor não encontrado."));

        validarPermissao(user, sensor.getId());

        PedidoConfiguracaoSensor pedido = new PedidoConfiguracaoSensor();
        pedido.setSensorId(sensor.getId());
        pedido.setSolicitadoPor(user.getId());
        pedido.setOrigem("APP");
        pedido.setEstado("PENDENTE");
        pedido.setIntervaloRapidoMs(request.intervaloRapidoMs());
        pedido.setIntervaloEstavelMs(request.intervaloEstavelMs());
        pedido.setDuracaoModoRapidoMs(request.duracaoModoRapidoMs());
        pedido.setDeltaSignificativo(request.deltaSignificativo());
        pedido.setMotivo(request.motivo());

        PedidoConfiguracaoSensor guardado = pedidoRepository.save(pedido);

        return new PedidoConfiguracaoSensorResponse(
                guardado.getId(),
                guardado.getEstado(),
                "Pedido enviado para aprovação."
        );
    }

    @Transactional(readOnly = true)
    public List<PedidoConfiguracaoSensorDTO> listarPendentes() {
        return pedidoRepository.findByEstadoOrderByCriadoEmDesc("PENDENTE")
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoConfiguracaoSensorDTO> listarMeusPedidos(AuthenticatedUser user) {
        return pedidoRepository.findBySolicitadoPorOrderByCriadoEmDesc(user.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public PedidoConfiguracaoSensorResponse aprovar(
            AuthenticatedUser user,
            Long pedidoId
    ) {
        PedidoConfiguracaoSensor pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (!"PENDENTE".equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalArgumentException("Este pedido já foi analisado.");
        }

        pedido.setEstado("APROVADO");
        pedido.setAnalisadoPor(user.getId());
        pedido.setAnalisadoEm(LocalDateTime.now());

        pedidoRepository.save(pedido);

        return new PedidoConfiguracaoSensorResponse(
                pedido.getId(),
                pedido.getEstado(),
                "Pedido aprovado."
        );
    }

    @Transactional
    public PedidoConfiguracaoSensorResponse rejeitar(
            AuthenticatedUser user,
            Long pedidoId,
            String respostaProfessor
    ) {
        PedidoConfiguracaoSensor pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (!"PENDENTE".equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalArgumentException("Este pedido já foi analisado.");
        }

        pedido.setEstado("REJEITADO");
        pedido.setAnalisadoPor(user.getId());
        pedido.setAnalisadoEm(LocalDateTime.now());
        pedido.setRespostaProfessor(respostaProfessor);

        pedidoRepository.save(pedido);

        return new PedidoConfiguracaoSensorResponse(
                pedido.getId(),
                pedido.getEstado(),
                "Pedido rejeitado."
        );
    }

    private void validarRequest(PedidoConfiguracaoSensorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Pedido inválido.");
        }

        if (request.sensorId() == null) {
            throw new IllegalArgumentException("Sensor obrigatório.");
        }

        validarObrigatorioPositivo(request.intervaloRapidoMs(), "Intervalo rápido");
        validarObrigatorioPositivo(request.intervaloEstavelMs(), "Intervalo estável");
        validarObrigatorioPositivo(request.duracaoModoRapidoMs(), "Duração do modo rápido");

        BigDecimal delta = request.deltaSignificativo();

        if (delta == null || delta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Delta significativo inválido.");
        }
    }

    private void validarObrigatorioPositivo(Integer valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException(campo + " inválido.");
        }
    }

    private void validarPermissao(AuthenticatedUser user, Long sensorId) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return;
        }

        long permitido = pedidoRepository.countSensorDisponivelParaUtilizador(
                sensorId,
                user.getId()
        );

        if (permitido == 0) {
            throw new SecurityException("Não tens permissão para criar pedidos para este sensor.");
        }
    }

    private SensorDisponivelDTO toSensorDisponivelDTO(Sensor sensor) {
        return new SensorDisponivelDTO(
                sensor.getId(),
                sensor.getNome(),
                sensor.getTipo(),
                sensor.getEstacao() != null ? sensor.getEstacao().getNome() : null
        );
    }

    private PedidoConfiguracaoSensorDTO toDTO(PedidoConfiguracaoSensor pedido) {
        Sensor sensor = null;

        if (pedido.getSensorId() != null) {
            sensor = sensorRepository.findById(pedido.getSensorId()).orElse(null);
        }

        return new PedidoConfiguracaoSensorDTO(
                pedido.getId(),
                pedido.getSensorId(),
                sensor != null ? sensor.getNome() : null,
                sensor != null ? sensor.getTipo() : null,
                sensor != null && sensor.getEstacao() != null ? sensor.getEstacao().getNome() : null,
                pedido.getSolicitadoPor(),
                pedido.getAnalisadoPor(),
                pedido.getOrigem(),
                pedido.getEstado(),
                pedido.getIntervaloRapidoMs(),
                pedido.getIntervaloEstavelMs(),
                pedido.getDuracaoModoRapidoMs(),
                pedido.getDeltaSignificativo(),
                pedido.getMotivo(),
                pedido.getRespostaProfessor(),
                pedido.getComandoId(),
                pedido.getCriadoEm(),
                pedido.getAnalisadoEm(),
                pedido.getAplicadoEm()
        );
    }
}