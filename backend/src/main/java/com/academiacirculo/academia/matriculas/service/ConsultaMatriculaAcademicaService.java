package com.academiacirculo.academia.matriculas.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.apoderados.repository.AlumnoVinculadoRepository;
import com.academiacirculo.academia.estudiantes.repository.MiPerfilEstudianteRepository;
import com.academiacirculo.academia.matriculas.dto.MatriculaAcademicaResponse;
import com.academiacirculo.academia.matriculas.repository.ConsultaMatriculaRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class ConsultaMatriculaAcademicaService {

    private final ConsultaMatriculaRepository matriculaRepository;
    private final MiPerfilEstudianteRepository estudianteRepository;
    private final AlumnoVinculadoRepository vinculoRepository;

    public ConsultaMatriculaAcademicaService(
            ConsultaMatriculaRepository matriculaRepository,
            MiPerfilEstudianteRepository estudianteRepository,
            AlumnoVinculadoRepository vinculoRepository) {

        this.matriculaRepository = matriculaRepository;
        this.estudianteRepository = estudianteRepository;
        this.vinculoRepository = vinculoRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ESTUDIANTE')")
    public MatriculaAcademicaResponse consultarPropia() {
        UUID usuarioId = obtenerUsuarioId();

        var estudiante = estudianteRepository
                .buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró tu perfil de estudiante."));

        return buscarMatricula(estudiante.estudianteId());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_APODERADO')")
    public MatriculaAcademicaResponse consultarDeAlumno(
            @NotNull UUID estudianteId) {

        UUID usuarioId = obtenerUsuarioId();

        vinculoRepository.buscarHijoPorId(usuarioId, estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el alumno solicitado."));

        return buscarMatricula(estudianteId);
    }

    private MatriculaAcademicaResponse buscarMatricula(
            UUID estudianteId) {

        return MatriculaAcademicaResponse.desde(
                matriculaRepository.buscarActiva(estudianteId)
                        .orElse(null));
    }

    private UUID obtenerUsuarioId() {
        var autenticacion = SecurityContextHolder.getContext()
                .getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {

            throw new AccessDeniedException(
                    "No se pudo identificar tu sesión.");
        }

        return principal.getUsuarioId();
    }
}