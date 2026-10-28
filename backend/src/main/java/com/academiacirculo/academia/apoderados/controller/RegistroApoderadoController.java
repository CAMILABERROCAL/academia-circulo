package com.academiacirculo.academia.apoderados.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.apoderados.dto.ApoderadoRegistradoResponse;
import com.academiacirculo.academia.apoderados.dto.RegistrarApoderadoRequest;
import com.academiacirculo.academia.apoderados.service.RegistroApoderadoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/secretaria/apoderados")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class RegistroApoderadoController {

    private final RegistroApoderadoService registroApoderadoService;

    public RegistroApoderadoController(
            RegistroApoderadoService registroApoderadoService) {

        this.registroApoderadoService = registroApoderadoService;
    }

    @PostMapping
    public ResponseEntity<ApoderadoRegistradoResponse> registrar(
            @Valid @RequestBody RegistrarApoderadoRequest solicitud) {

        var respuesta = registroApoderadoService.registrar(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(respuesta);
    }
}