package com.academiacirculo.academia.matriculas.controller;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.matriculas.dto.ConsultaMatriculaResponse;
import com.academiacirculo.academia.matriculas.service.ConsultaMatriculaService;

@RestController
@RequestMapping("/api/secretaria/estudiantes")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class ConsultaMatriculaController {

    private final ConsultaMatriculaService service;

    public ConsultaMatriculaController(
            ConsultaMatriculaService service) {
        this.service = service;
    }

    @GetMapping("/{estudianteId}/matricula-activa")
    public ResponseEntity<ConsultaMatriculaResponse> consultar(
            @PathVariable("estudianteId") UUID estudianteId) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.consultar(estudianteId));
    }
}