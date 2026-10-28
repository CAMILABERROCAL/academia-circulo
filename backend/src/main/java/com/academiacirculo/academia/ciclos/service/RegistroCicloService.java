package com.academiacirculo.academia.ciclos.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.ciclos.dto.CicloResponse;
import com.academiacirculo.academia.ciclos.dto.CrearCicloRequest;
import com.academiacirculo.academia.ciclos.repository.RegistroCicloRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RegistroCicloService {

    private final RegistroCicloRepository registroRepository;

    public RegistroCicloService(
            RegistroCicloRepository registroRepository) {

        this.registroRepository = registroRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CicloResponse crear(
            @NotNull @Valid CrearCicloRequest solicitud) {

        UUID responsableId = obtenerResponsableId();

        String codigo = solicitud.codigo()
                .toUpperCase(Locale.ROOT);

        if (registroRepository.existeCodigo(codigo)) {
            throw new ConflictoDeDatosException(
                    "El código del ciclo ya está registrado.");
        }

        UUID cicloId = registroRepository.crear(codigo, solicitud);

        registroRepository.registrarAuditoria(
                responsableId,
                cicloId);

        return new CicloResponse(
                cicloId,
                codigo,
                solicitud.nombre().strip(),
                solicitud.fechaInicio(),
                solicitud.fechaFin(),
                solicitud.duracionMeses(),
                "PLANIFICADO");
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