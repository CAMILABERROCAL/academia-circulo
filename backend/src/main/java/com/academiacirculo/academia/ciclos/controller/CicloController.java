package com.academiacirculo.academia.ciclos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.ciclos.dto.CicloResponse;
import com.academiacirculo.academia.ciclos.dto.TurnoResponse;
import com.academiacirculo.academia.ciclos.service.CicloService;
import com.academiacirculo.academia.ciclos.service.ConsultaTurnoService;

@RestController
@RequestMapping("/api/secretaria/ciclos")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class CicloController {

    private final CicloService cicloService;
    private final ConsultaTurnoService turnoService;

    public CicloController(
            CicloService cicloService,
            ConsultaTurnoService turnoService) {
        this.cicloService = cicloService;
        this.turnoService = turnoService;
    }

    @GetMapping
    public ResponseEntity<List<CicloResponse>> listar(
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20") int tamanio) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(cicloService.listar(pagina, tamanio));
    }

    @GetMapping("/{cicloId}/turnos")
    public ResponseEntity<List<TurnoResponse>> listarTurnos(
            @PathVariable("cicloId") UUID cicloId) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(turnoService.listar(cicloId));
    }
}