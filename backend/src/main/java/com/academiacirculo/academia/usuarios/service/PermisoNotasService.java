package com.academiacirculo.academia.usuarios.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.usuarios.dto.CambiarPermisoNotasRequest;
import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.EstadoUsuario;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.AuditoriaPermisosRepository;
import com.academiacirculo.academia.usuarios.repository.UsuarioPermisoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class PermisoNotasService {

    private static final String PERMISO_GESTIONAR = "NOTAS_PERMISOS_GESTIONAR";

    private final EntityManager entityManager;
    private final UsuarioPermisoRepository permisosRepository;
    private final AuditoriaPermisosRepository auditoriaRepository;

    public PermisoNotasService(
            EntityManager entityManager,
            UsuarioPermisoRepository permisosRepository,
            AuditoriaPermisosRepository auditoriaRepository) {

        this.entityManager = entityManager;
        this.permisosRepository = permisosRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    @PreAuthorize("""
            hasAuthority('ROLE_DIRECTOR')
            and hasAuthority('NOTAS_PERMISOS_GESTIONAR')
            """)
    public Resultado cambiarPermiso(
            @NotNull UUID usuarioId,
            @NotNull @Valid CambiarPermisoNotasRequest solicitud,
            boolean otorgar) {

        var autenticacion = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacion == null
                || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión como director.");
        }

        UUID directorId = principal.getUsuarioId();

        if (directorId.equals(usuarioId)) {
            throw new AccessDeniedException(
                    "No puedes modificar tu propio permiso.");
        }

        Usuario director = entityManager.find(
                Usuario.class,
                directorId);

        validarDirector(director);

        // Serializa los cambios de permiso sobre el mismo usuario.
        Usuario destinatario = entityManager.find(
                Usuario.class,
                usuarioId,
                LockModeType.PESSIMISTIC_WRITE);

        if (destinatario == null) {
            throw new IllegalArgumentException(
                    "El usuario indicado no existe.");
        }

        if (!tieneRol(destinatario, CodigoRol.ROLE_ADMIN)) {
            throw new IllegalArgumentException(
                    "Este permiso solo se gestiona para administradores.");
        }

        if (tieneRol(destinatario, CodigoRol.ROLE_DIRECTOR)) {
            throw new AccessDeniedException(
                    "Esta operación no permite modificar a otro director.");
        }

        if (tieneRol(destinatario, CodigoRol.ROLE_SECRETARIA)) {
            throw new IllegalArgumentException(
                    "Este usuario ya recibe el permiso por su rol de secretaría.");
        }

        if (otorgar
                && destinatario.getEstado() != EstadoUsuario.ACTIVO) {

            throw new IllegalArgumentException(
                    "Solo puedes otorgar el permiso a un usuario activo.");
        }

        String motivo = solicitud.motivo().strip();
        boolean modificado;

        if (otorgar) {
            modificado = permisosRepository.otorgarPermisoNotas(
                    usuarioId,
                    directorId,
                    motivo);

            if (modificado) {
                auditoriaRepository.registrarOtorgamiento(
                        directorId,
                        usuarioId,
                        motivo);
            }
        } else {
            modificado = permisosRepository.revocarPermisoNotas(
                    usuarioId);

            if (modificado) {
                auditoriaRepository.registrarRevocacion(
                        directorId,
                        usuarioId,
                        motivo);
            }
        }

        return new Resultado(
                destinatario.getNombreUsuario(),
                modificado);
    }

    private void validarDirector(Usuario director) {
        if (director == null
                || director.getEstado() != EstadoUsuario.ACTIVO
                || director.isDebeCambiarPassword()
                || !tieneRol(director, CodigoRol.ROLE_DIRECTOR)) {

            throw new AccessDeniedException(
                    "El usuario no está autorizado para gestionar permisos.");
        }

        if (director.getBloqueadoHasta() != null
                && director.getBloqueadoHasta().isAfter(Instant.now())) {

            throw new AccessDeniedException(
                    "La cuenta del director está bloqueada.");
        }

        boolean permisoPorRol = director.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .anyMatch(permiso -> PERMISO_GESTIONAR.equals(
                        permiso.getCodigo().name()));

        boolean autorizado = permisoPorRol
                || permisosRepository
                        .buscarCodigosPorUsuarioId(director.getId())
                        .contains(PERMISO_GESTIONAR);

        if (!autorizado) {
            throw new AccessDeniedException(
                    "No tienes el permiso para gestionar la edición de notas.");
        }
    }

    private boolean tieneRol(Usuario usuario, CodigoRol codigo) {
        return usuario.getRoles().stream()
                .anyMatch(rol -> rol.getCodigo() == codigo);
    }

    public record Resultado(
            String nombreUsuario,
            boolean modificado) {
    }
}