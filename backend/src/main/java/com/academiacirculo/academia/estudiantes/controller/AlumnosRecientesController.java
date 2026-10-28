package com.academiacirculo.academia.estudiantes.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.estudiantes.dto.AlumnoRecienteResponse;
import com.academiacirculo.academia.estudiantes.service.AlumnosRecientesService;

@RestController
@RequestMapping("/api/secretaria/estudiantes")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class AlumnosRecientesController {

    private final AlumnosRecientesService service;

    public AlumnosRecientesController(
            AlumnosRecientesService service) {
        this.service = service;
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<AlumnoRecienteResponse>> consultar() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.consultar());
    }
}