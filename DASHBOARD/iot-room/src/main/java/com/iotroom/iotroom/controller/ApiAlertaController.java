package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.AlertaSensorDTO;
import com.iotroom.iotroom.model.AlertaSensor;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorAlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class ApiAlertaController {

    private final ProfessorAlertaService professorAlertaService;

    public ApiAlertaController(ProfessorAlertaService professorAlertaService) {
        this.professorAlertaService = professorAlertaService;
    }

    @GetMapping
    public List<AlertaSensorDTO> listar(Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        return professorAlertaService.listarAlertas(professorId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PostMapping("/{id}/marcar-lido")
    public ResponseEntity<Void> marcarLido(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);
        professorAlertaService.marcarLido(id, professorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolver")
    public ResponseEntity<Void> resolver(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);
        professorAlertaService.resolver(id, professorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ignorar")
    public ResponseEntity<Void> ignorar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);
        professorAlertaService.ignorar(id, professorId);
        return ResponseEntity.noContent().build();
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }

    private AlertaSensorDTO toDTO(AlertaSensor alerta) {
        return new AlertaSensorDTO(
                alerta.getId(),
                alerta.getTipoSensor(),
                alerta.getValorLido(),
                alerta.getValorMin(),
                alerta.getValorMax(),
                alerta.getTitulo(),
                alerta.getMensagem(),
                alerta.getSeveridade(),
                alerta.getEstado(),
                alerta.getCriadoEm(),
                alerta.getLidoEm(),
                alerta.getResolvidoEm()
        );
    }
}