package com.academiacirculo.academia.estudiantes.controller;

import java.net.URI;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.estudiantes.dto.FichaEstudianteResponse;
import com.academiacirculo.academia.estudiantes.dto.RegistrarEstudianteRequest;
import com.academiacirculo.academia.estudiantes.service.RegistroEstudianteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/secretaria/estudiantes")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class RegistroEstudianteController {

    private final RegistroEstudianteService registroEstudianteService;

    public RegistroEstudianteController(
            RegistroEstudianteService registroEstudianteService) {

        this.registroEstudianteService = registroEstudianteService;
    }

    @PostMapping
    public ResponseEntity<FichaEstudianteResponse> registrar(
            @Valid @RequestBody RegistrarEstudianteRequest solicitud) {

        FichaEstudianteResponse ficha = registroEstudianteService.registrar(solicitud);

        URI ubicacion = URI.create(
                "/api/secretaria/estudiantes/"
                        + ficha.estudianteId()
                        + "/ficha");

        return ResponseEntity.created(ubicacion)
                .cacheControl(CacheControl.noStore())
                .body(ficha);
    }
}