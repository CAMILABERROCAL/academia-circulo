package com.academiacirculo.academia.estudiantes.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.estudiantes.dto.VincularApoderadoRequest;
import com.academiacirculo.academia.estudiantes.repository.FichaEstudianteRepository;
import com.academiacirculo.academia.estudiantes.repository.VinculoApoderadoRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;
import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.EstadoUsuario;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class VinculoApoderadoService {

    private final FichaEstudianteRepository fichaRepository;
    private final VinculoApoderadoRepository vinculoRepository;
    private final UsuarioRepository usuarioRepository;

    public VinculoApoderadoService(
            FichaEstudianteRepository fichaRepository,
            VinculoApoderadoRepository vinculoRepository,
            UsuarioRepository usuarioRepository) {

        this.fichaRepository = fichaRepository;
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public void vincular(
            @NotNull UUID estudianteId,
            @NotNull @Valid VincularApoderadoRequest solicitud) {

        UUID responsableId = obtenerResponsableId();

        // Serializa las vinculaciones concurrentes del mismo estudiante.
        UUID usuarioEstudianteId = fichaRepository
                .bloquearYBuscarUsuarioId(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El estudiante no existe."));

        if (fichaRepository.tieneApoderado(estudianteId)) {
            throw new ConflictoDeDatosException(
                    "El estudiante ya tiene un apoderado vinculado.");
        }

        UUID usuarioApoderadoId = vinculoRepository
                .buscarUsuarioApoderado(solicitud.apoderadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El apoderado no existe."));

        if (usuarioEstudianteId.equals(usuarioApoderadoId)) {
            throw new ConflictoDeDatosException(
                    "Un estudiante no puede ser su propio apoderado.");
        }

        Usuario estudiante = usuarioRepository.findById(usuarioEstudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La cuenta del estudiante no existe."));

        Usuario apoderado = usuarioRepository.findById(usuarioApoderadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La cuenta del apoderado no existe."));

        boolean esEstudiante = estudiante.getRoles().stream()
                .anyMatch(rol -> rol.getCodigo() == CodigoRol.ROLE_ESTUDIANTE);

        boolean esApoderado = apoderado.getRoles().stream()
                .anyMatch(rol -> rol.getCodigo() == CodigoRol.ROLE_APODERADO);

        if (!esEstudiante || !esApoderado) {
            throw new ConflictoDeDatosException(
                    "Las cuentas no tienen los roles requeridos.");
        }

        if (apoderado.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ConflictoDeDatosException(
                    "La cuenta del apoderado debe estar activa.");
        }

        vinculoRepository.vincular(
                estudianteId,
                solicitud.apoderadoId(),
                solicitud.parentesco());

        vinculoRepository.registrarAuditoria(
                responsableId,
                estudianteId,
                solicitud.apoderadoId(),
                solicitud.parentesco());
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