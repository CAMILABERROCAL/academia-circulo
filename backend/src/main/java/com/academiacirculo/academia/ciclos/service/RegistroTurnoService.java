package com.academiacirculo.academia.ciclos.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.ciclos.dto.CrearTurnoRequest;
import com.academiacirculo.academia.ciclos.repository.RegistroTurnoRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RegistroTurnoService {

    private final RegistroTurnoRepository repository;

    public RegistroTurnoService(RegistroTurnoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_DIRECTOR')")
    public UUID crear(
            @NotNull UUID cicloId,
            @NotNull @Valid CrearTurnoRequest solicitud) {

        UUID responsableId = obtenerResponsableId();
        var dias = new HashSet<Integer>();

        for (var horario : solicitud.horarios()) {
            if (!dias.add(horario.diaSemana())) {
                throw new IllegalArgumentException(
                        "No puedes repetir un día dentro del mismo turno.");
            }

            if (!horario.horaSalida().isAfter(horario.horaEntrada())) {
                throw new IllegalArgumentException(
                        "La salida debe ser posterior a la entrada.");
            }

            if (horario.horaEntrada().getSecond() != 0
                    || horario.horaEntrada().getNano() != 0
                    || horario.horaSalida().getSecond() != 0
                    || horario.horaSalida().getNano() != 0) {
                throw new IllegalArgumentException(
                        "Indica las horas con precisión de minutos, sin segundos.");
            }
        }

        String estadoCiclo = repository.bloquearCiclo(cicloId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el ciclo solicitado."));

        if (!"PLANIFICADO".equals(estadoCiclo)
                && !"ACTIVO".equals(estadoCiclo)) {
            throw new ConflictoDeDatosException(
                    "Solo se pueden crear turnos en ciclos planificados o activos.");
        }

        String codigo = solicitud.codigo().toUpperCase(Locale.ROOT);

        if (repository.existeCodigo(cicloId, codigo)) {
            throw new ConflictoDeDatosException(
                    "El código del turno ya está registrado en este ciclo.");
        }

        UUID turnoId = repository.crearTurno(
                cicloId,
                codigo,
                solicitud.nombre().strip());

        for (var horario : solicitud.horarios()) {
            repository.crearHorario(turnoId, horario);
        }

        repository.registrarAuditoria(
                responsableId,
                cicloId,
                turnoId,
                codigo,
                solicitud.horarios().size());

        return turnoId;
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