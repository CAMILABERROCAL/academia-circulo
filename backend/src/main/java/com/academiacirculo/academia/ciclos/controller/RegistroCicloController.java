package com.academiacirculo.academia.ciclos.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.ciclos.dto.CicloResponse;
import com.academiacirculo.academia.ciclos.dto.CrearCicloRequest;
import com.academiacirculo.academia.ciclos.service.CicloService;
import com.academiacirculo.academia.ciclos.service.RegistroCicloService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/ciclos")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RegistroCicloController {

    private final RegistroCicloService registroCicloService;
    private final CicloService cicloService;

    public RegistroCicloController(
            RegistroCicloService registroCicloService,
            CicloService cicloService) {

        this.registroCicloService = registroCicloService;
        this.cicloService = cicloService;
    }

    @PostMapping
    public ResponseEntity<CicloResponse> crear(
            @Valid @RequestBody CrearCicloRequest solicitud) {

        CicloResponse ciclo = registroCicloService.crear(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(ciclo);
    }

    @GetMapping
    public ResponseEntity<List<CicloResponse>> listar(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20") int tamanio) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(cicloService.listar(pagina, tamanio));
    }
}