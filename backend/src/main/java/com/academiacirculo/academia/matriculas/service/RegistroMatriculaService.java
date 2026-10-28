package com.academiacirculo.academia.matriculas.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.matriculas.dto.MatriculaRegistradaResponse;
import com.academiacirculo.academia.matriculas.dto.RegistrarMatriculaRequest;
import com.academiacirculo.academia.matriculas.repository.RegistroMatriculaRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RegistroMatriculaService {

    private static final ZoneId ZONA_ACADEMIA = ZoneId.of("America/Lima");

    private final RegistroMatriculaRepository repository;

    public RegistroMatriculaService(
            RegistroMatriculaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public MatriculaRegistradaResponse registrar(
            @NotNull @Valid RegistrarMatriculaRequest solicitud) {

        UUID responsableId = obtenerResponsableId();

        boolean estudianteHabilitado = repository
                .bloquearEstudiante(solicitud.estudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el estudiante solicitado."));

        if (!estudianteHabilitado) {
            throw new ConflictoDeDatosException(
                    "El estudiante y su cuenta deben estar activos.");
        }

        String estadoCiclo = repository
                .bloquearCiclo(solicitud.cicloId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el ciclo solicitado."));

        if (!"PLANIFICADO".equals(estadoCiclo)
                && !"ACTIVO".equals(estadoCiclo)) {
            throw new ConflictoDeDatosException(
                    "Solo puedes matricular en ciclos planificados o activos.");
        }

        String estadoTurno = repository
                .bloquearTurno(
                        solicitud.turnoId(),
                        solicitud.cicloId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el turno dentro del ciclo seleccionado."));

        if (!"ACTIVO".equals(estadoTurno)) {
            throw new ConflictoDeDatosException(
                    "El turno seleccionado debe estar activo.");
        }

        if (!repository.tieneHorarios(solicitud.turnoId())) {
            throw new ConflictoDeDatosException(
                    "El turno seleccionado todavía no tiene horarios.");
        }

        if (repository.tieneMatriculaActiva(
                solicitud.estudianteId())) {
            throw new ConflictoDeDatosException(
                    "El estudiante ya tiene una matrícula activa.");
        }

        if (repository.existeMatriculaEnCiclo(
                solicitud.estudianteId(),
                solicitud.cicloId())) {
            throw new ConflictoDeDatosException(
                    "El estudiante ya tiene una matrícula registrada en este ciclo.");
        }

        String codigo = "MAT-" + UUID.randomUUID();
        LocalDate fecha = LocalDate.now(ZONA_ACADEMIA);

        UUID matriculaId = repository.registrar(
                codigo,
                solicitud.estudianteId(),
                solicitud.cicloId(),
                solicitud.turnoId(),
                fecha);

        repository.registrarAuditoria(
                responsableId,
                matriculaId,
                solicitud.estudianteId(),
                solicitud.cicloId(),
                solicitud.turnoId());

        return new MatriculaRegistradaResponse(
                matriculaId,
                codigo,
                solicitud.estudianteId(),
                solicitud.cicloId(),
                solicitud.turnoId(),
                fecha,
                "ACTIVA");
    }

    private UUID obtenerResponsableId() {
        var autenticacion = SecurityContextHolder.getContext()
                .getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AccessDeniedException(
                    "No se pudo identificar al responsable.");
        }

        return principal.getUsuarioId();
    }
}