package com.academiacirculo.academia.estudiantes.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.estudiantes.dto.MiPerfilEstudianteResponse;
import com.academiacirculo.academia.estudiantes.repository.MiPerfilEstudianteRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

@RestController
@RequestMapping("/api/estudiante")
@PreAuthorize("hasAuthority('ROLE_ESTUDIANTE')")
public class MiPerfilEstudianteController {

    private final MiPerfilEstudianteRepository repository;

    public MiPerfilEstudianteController(
            MiPerfilEstudianteRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/mi-perfil")
    public ResponseEntity<MiPerfilEstudianteResponse> consultar(
            @AuthenticationPrincipal UsuarioPrincipal principal) {

        if (principal == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión como estudiante.");
        }

        MiPerfilEstudianteResponse perfil = repository
                .buscarPorUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un perfil de estudiante para tu cuenta."));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(perfil);
    }
}