package com.academiacirculo.academia.ciclos.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.ciclos.dto.CrearTurnoRequest;
import com.academiacirculo.academia.ciclos.service.RegistroTurnoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/director/ciclos")
@PreAuthorize("hasAuthority('ROLE_DIRECTOR')")
public class RegistroTurnoController {

    private final RegistroTurnoService service;

    public RegistroTurnoController(RegistroTurnoService service) {
        this.service = service;
    }

    @PostMapping("/{cicloId}/turnos")
    public ResponseEntity<Map<String, UUID>> crear(
            @PathVariable("cicloId") UUID cicloId,
            @Valid @RequestBody CrearTurnoRequest solicitud) {

        UUID turnoId = service.crear(cicloId, solicitud);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("turnoId", turnoId));
    }
}