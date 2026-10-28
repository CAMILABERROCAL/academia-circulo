package com.academiacirculo.academia.estudiantes.controller;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.estudiantes.dto.VincularApoderadoRequest;
import com.academiacirculo.academia.estudiantes.service.VinculoApoderadoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/secretaria/estudiantes")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class VinculoApoderadoController {

    private final VinculoApoderadoService vinculoApoderadoService;

    public VinculoApoderadoController(
            VinculoApoderadoService vinculoApoderadoService) {

        this.vinculoApoderadoService = vinculoApoderadoService;
    }

    @PostMapping("/{estudianteId}/apoderado")
    public ResponseEntity<Void> vincular(
            @PathVariable("estudianteId") UUID estudianteId,
            @Valid @RequestBody VincularApoderadoRequest solicitud) {

        vinculoApoderadoService.vincular(estudianteId, solicitud);

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}