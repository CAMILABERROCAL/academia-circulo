package com.academiacirculo.academia.apoderados.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.apoderados.dto.AlumnoVinculadoResponse;
import com.academiacirculo.academia.apoderados.service.ConsultaAlumnosApoderadoService;

@RestController
@RequestMapping("/api/secretaria/apoderados")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class ConsultaAlumnosApoderadoController {

    private final ConsultaAlumnosApoderadoService service;

    public ConsultaAlumnosApoderadoController(
            ConsultaAlumnosApoderadoService service) {
        this.service = service;
    }

    @GetMapping("/{apoderadoId}/alumnos")
    public ResponseEntity<List<AlumnoVinculadoResponse>> consultar(
            @PathVariable("apoderadoId") UUID apoderadoId) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.consultar(apoderadoId));
    }
}