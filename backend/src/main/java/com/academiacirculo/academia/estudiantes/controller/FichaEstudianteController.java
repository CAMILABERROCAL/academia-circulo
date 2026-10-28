package com.academiacirculo.academia.estudiantes.controller;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.auth.service.SesionesUsuarioService;
import com.academiacirculo.academia.estudiantes.dto.ActualizarFichaEstudianteRequest;
import com.academiacirculo.academia.estudiantes.service.FichaEstudianteService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import com.academiacirculo.academia.estudiantes.dto.FichaEstudianteResponse;
import org.springframework.web.bind.annotation.RequestParam;
import com.academiacirculo.academia.estudiantes.dto.PaginaEstudiantesResponse;
import java.util.List;
import com.academiacirculo.academia.estudiantes.dto.HistorialFichaResponse;

@RestController
@RequestMapping("/api/secretaria/estudiantes")
@PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
public class FichaEstudianteController {

        private final FichaEstudianteService fichaEstudianteService;
        private final SesionesUsuarioService sesionesUsuarioService;

        public FichaEstudianteController(
                        FichaEstudianteService fichaEstudianteService,
                        SesionesUsuarioService sesionesUsuarioService) {

                this.fichaEstudianteService = fichaEstudianteService;
                this.sesionesUsuarioService = sesionesUsuarioService;
        }

        @PutMapping("/{estudianteId}/ficha")
        public ResponseEntity<Void> actualizarFicha(
                        @PathVariable("estudianteId") UUID estudianteId,
                        @Valid @RequestBody ActualizarFichaEstudianteRequest solicitud) {

                String nombreUsuario = fichaEstudianteService.actualizar(
                                estudianteId,
                                solicitud);

                sesionesUsuarioService.cerrarTodasLasSesiones(
                                nombreUsuario);

                return ResponseEntity.noContent()
                                .cacheControl(CacheControl.noStore())
                                .build();
        }

        @GetMapping("/{estudianteId}/ficha")
        public ResponseEntity<FichaEstudianteResponse> consultarFicha(
                        @PathVariable("estudianteId") UUID estudianteId) {

                return ResponseEntity.ok()
                                .cacheControl(CacheControl.noStore())
                                .body(fichaEstudianteService.consultar(estudianteId));
        }

        @GetMapping
        public ResponseEntity<PaginaEstudiantesResponse> listarEstudiantes(
                        @RequestParam(name = "pagina", defaultValue = "0") int pagina,
                        @RequestParam(name = "tamanio", defaultValue = "20") int tamanio,
                        @RequestParam(name = "busqueda", defaultValue = "") String busqueda) {

                return ResponseEntity.ok()
                                .cacheControl(CacheControl.noStore())
                                .body(fichaEstudianteService.listar(
                                                pagina,
                                                tamanio,
                                                busqueda));
        }

        @GetMapping("/{estudianteId}/ficha/historial")
        public ResponseEntity<List<HistorialFichaResponse>> consultarHistorial(
                        @PathVariable("estudianteId") UUID estudianteId,
                        @RequestParam(name = "pagina", defaultValue = "0") int pagina,
                        @RequestParam(name = "tamanio", defaultValue = "20") int tamanio) {

                return ResponseEntity.ok()
                                .cacheControl(CacheControl.noStore())
                                .body(fichaEstudianteService.consultarHistorial(
                                                estudianteId,
                                                pagina,
                                                tamanio));
        }
}