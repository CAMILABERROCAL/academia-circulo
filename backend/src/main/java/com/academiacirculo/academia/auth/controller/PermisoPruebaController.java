package com.academiacirculo.academia.auth.controller;

import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pruebas/permisos")
public class PermisoPruebaController {

    @GetMapping("/notas-editar")
    @PreAuthorize("hasAuthority('NOTAS_EDITAR')")
    public ResponseEntity<Map<String, String>> comprobarEdicionNotas() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "mensaje", "Acceso permitido para editar notas.",
                        "permiso", "NOTAS_EDITAR"));
    }
}