package com.academiacirculo.academia.estudiantes.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.estudiantes.dto.IndicadoresAlumnosResponse;
import com.academiacirculo.academia.estudiantes.service.IndicadoresAlumnosService;

@RestController
@RequestMapping("/api/secretaria/estudiantes")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class IndicadoresAlumnosController {

    private final IndicadoresAlumnosService service;

    public IndicadoresAlumnosController(
            IndicadoresAlumnosService service) {
        this.service = service;
    }

    @GetMapping("/indicadores")
    public ResponseEntity<IndicadoresAlumnosResponse> consultar() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.consultar());
    }
}