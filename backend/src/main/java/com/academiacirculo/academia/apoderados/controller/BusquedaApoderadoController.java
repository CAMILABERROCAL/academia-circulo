package com.academiacirculo.academia.apoderados.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.apoderados.dto.ApoderadoResumenResponse;
import com.academiacirculo.academia.apoderados.service.BusquedaApoderadoService;

@RestController
@RequestMapping("/api/secretaria/apoderados")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class BusquedaApoderadoController {

    private final BusquedaApoderadoService busquedaApoderadoService;

    public BusquedaApoderadoController(
            BusquedaApoderadoService busquedaApoderadoService) {

        this.busquedaApoderadoService = busquedaApoderadoService;
    }

    @GetMapping
    public ResponseEntity<List<ApoderadoResumenResponse>> buscar(
            @RequestParam(name = "busqueda", defaultValue = "") String busqueda,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20") int tamanio) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(busquedaApoderadoService.buscar(
                        busqueda,
                        pagina,
                        tamanio));
    }
}