package com.academiacirculo.academia.usuarios.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.usuarios.dto.CrearUsuarioRequest;
import com.academiacirculo.academia.usuarios.dto.UsuarioResponse;
import com.academiacirculo.academia.usuarios.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponse> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest solicitud) {

        UsuarioResponse usuarioCreado = usuarioService.crear(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(usuarioCreado);
    }
}