package com.academiacirculo.academia.matriculas.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.matriculas.dto.MatriculaRegistradaResponse;
import com.academiacirculo.academia.matriculas.dto.RegistrarMatriculaRequest;
import com.academiacirculo.academia.matriculas.service.RegistroMatriculaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/secretaria/matriculas")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class RegistroMatriculaController {

    private final RegistroMatriculaService service;

    public RegistroMatriculaController(
            RegistroMatriculaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MatriculaRegistradaResponse> registrar(
            @Valid @RequestBody RegistrarMatriculaRequest solicitud) {

        MatriculaRegistradaResponse respuesta = service.registrar(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(respuesta);
    }
}