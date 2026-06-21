package com.iotroom.auth.controller;

import com.iotroom.auth.dto.DeviceResponseDTO;
import com.iotroom.auth.model.Utilizador;
import com.iotroom.auth.service.DeviceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/devices")
public class DeviceRestController {

    private final DeviceService deviceService;

    public DeviceRestController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> listar(Authentication authentication) {
        Utilizador utilizador = (Utilizador) authentication.getPrincipal();

        return ResponseEntity.ok(
                deviceService.listarDispositivos(utilizador.getId())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Utilizador utilizador = (Utilizador) authentication.getPrincipal();

        deviceService.desativarDispositivo(utilizador, id);

        return ResponseEntity.noContent().build();
    }
}