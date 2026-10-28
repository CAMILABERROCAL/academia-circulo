package com.academiacirculo.academia.matriculas.controller;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.matriculas.dto.MatriculaAcademicaResponse;
import com.academiacirculo.academia.matriculas.service.ConsultaMatriculaAcademicaService;

@RestController
public class ConsultaMatriculaAcademicaController {

    private final ConsultaMatriculaAcademicaService service;

    public ConsultaMatriculaAcademicaController(
            ConsultaMatriculaAcademicaService service) {
        this.service = service;
    }

    @GetMapping("/api/estudiante/mi-matricula")
    @PreAuthorize("hasAuthority('ROLE_ESTUDIANTE')")
    public ResponseEntity<MatriculaAcademicaResponse> consultarPropia() {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.consultarPropia());
    }

    @GetMapping("/api/apoderado/hijos/{estudianteId}/matricula-activa")
    @PreAuthorize("hasAuthority('ROLE_APODERADO')")
    public ResponseEntity<MatriculaAcademicaResponse> consultarDeAlumno(
            @PathVariable("estudianteId") UUID estudianteId) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.consultarDeAlumno(estudianteId));
    }
}