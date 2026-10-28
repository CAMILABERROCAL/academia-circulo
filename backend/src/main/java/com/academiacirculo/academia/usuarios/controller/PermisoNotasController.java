package com.academiacirculo.academia.usuarios.controller;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.auth.service.SesionesUsuarioService;
import com.academiacirculo.academia.usuarios.dto.CambiarPermisoNotasRequest;
import com.academiacirculo.academia.usuarios.service.PermisoNotasService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/director/usuarios")
@PreAuthorize("""
        hasAuthority('ROLE_DIRECTOR')
        and hasAuthority('NOTAS_PERMISOS_GESTIONAR')
        """)
public class PermisoNotasController {

    private final PermisoNotasService permisoNotasService;
    private final SesionesUsuarioService sesionesUsuarioService;

    public PermisoNotasController(
            PermisoNotasService permisoNotasService,
            SesionesUsuarioService sesionesUsuarioService) {

        this.permisoNotasService = permisoNotasService;
        this.sesionesUsuarioService = sesionesUsuarioService;
    }

    @PostMapping("/{usuarioId}/permisos/notas-editar/otorgar")
    public ResponseEntity<Void> otorgar(
            @PathVariable("usuarioId") UUID usuarioId,
            @Valid @RequestBody CambiarPermisoNotasRequest solicitud) {

        var resultado = permisoNotasService.cambiarPermiso(
                usuarioId,
                solicitud,
                true);

        sesionesUsuarioService.cerrarTodasLasSesiones(
                resultado.nombreUsuario());

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }

    @PostMapping("/{usuarioId}/permisos/notas-editar/revocar")
    public ResponseEntity<Void> revocar(
            @PathVariable("usuarioId") UUID usuarioId,
            @Valid @RequestBody CambiarPermisoNotasRequest solicitud) {

        var resultado = permisoNotasService.cambiarPermiso(
                usuarioId,
                solicitud,
                false);

        sesionesUsuarioService.cerrarTodasLasSesiones(
                resultado.nombreUsuario());

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}