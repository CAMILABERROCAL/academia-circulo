package com.academiacirculo.academia.apoderados.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.apoderados.dto.AlumnoVinculadoResponse;
import com.academiacirculo.academia.apoderados.repository.AlumnoVinculadoRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

@RestController
@RequestMapping("/api/apoderado")
@PreAuthorize("hasAuthority('ROLE_APODERADO')")
public class ApoderadoController {

    private final AlumnoVinculadoRepository alumnoVinculadoRepository;

    public ApoderadoController(
            AlumnoVinculadoRepository alumnoVinculadoRepository) {

        this.alumnoVinculadoRepository = alumnoVinculadoRepository;
    }

    @GetMapping("/hijos")
    public ResponseEntity<List<AlumnoVinculadoResponse>> obtenerHijos(
            @AuthenticationPrincipal UsuarioPrincipal principal) {

        UUID usuarioApoderadoId = obtenerUsuarioId(principal);

        List<AlumnoVinculadoResponse> hijos = alumnoVinculadoRepository.buscarPorUsuarioApoderado(
                usuarioApoderadoId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(hijos);
    }

    @GetMapping("/hijos/{estudianteId}")
    public ResponseEntity<AlumnoVinculadoResponse> obtenerHijo(
            @PathVariable("estudianteId") UUID estudianteId,
            @AuthenticationPrincipal UsuarioPrincipal principal) {

        UUID usuarioApoderadoId = obtenerUsuarioId(principal);

        AlumnoVinculadoResponse hijo = alumnoVinculadoRepository.buscarHijoPorId(
                usuarioApoderadoId,
                estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el alumno solicitado."));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(hijo);
    }

    private UUID obtenerUsuarioId(UsuarioPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión como apoderado.");
        }

        return principal.getUsuarioId();
    }
}