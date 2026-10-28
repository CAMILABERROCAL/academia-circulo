package com.academiacirculo.academia.apoderados.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.apoderados.dto.ApoderadoRegistradoResponse;
import com.academiacirculo.academia.apoderados.dto.RegistrarApoderadoRequest;
import com.academiacirculo.academia.apoderados.repository.RegistroApoderadoRepository;
import com.academiacirculo.academia.auth.dto.TipoAcceso;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.usuarios.dto.CrearUsuarioRequest;
import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.TipoDocumento;
import com.academiacirculo.academia.usuarios.repository.AccesoCelularRepository;
import com.academiacirculo.academia.usuarios.service.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RegistroApoderadoService {

    private final UsuarioService usuarioService;
    private final RegistroApoderadoRepository registroRepository;
    private final AccesoCelularRepository accesoCelularRepository;

    public RegistroApoderadoService(
            UsuarioService usuarioService,
            RegistroApoderadoRepository registroRepository,
            AccesoCelularRepository accesoCelularRepository) {

        this.usuarioService = usuarioService;
        this.registroRepository = registroRepository;
        this.accesoCelularRepository = accesoCelularRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public ApoderadoRegistradoResponse registrar(
            @NotNull @Valid RegistrarApoderadoRequest solicitud) {

        UUID responsableId = obtenerResponsableId();

        String celular = solicitud.celular();

        if (celular.startsWith("+51")) {
            celular = celular.substring(3);
        }

        registroRepository.bloquearRegistroPorCelular(celular);

        boolean celularOcupado = !accesoCelularRepository
                .buscarCandidatos(TipoAcceso.APODERADO, celular, null)
                .isEmpty();

        if (celularOcupado) {
            throw new ConflictoDeDatosException(
                    "Ya existe una cuenta de apoderado con ese celular. "
                            + "Comprueba si corresponde al apoderado que buscas.");
        }

        String nombreUsuario = "apoderado."
                + UUID.randomUUID().toString().replace("-", "");

        var cuenta = usuarioService.crear(new CrearUsuarioRequest(
                nombreUsuario,
                null,
                solicitud.passwordTemporal(),
                solicitud.nombres(),
                solicitud.apellidos(),
                TipoDocumento.DNI,
                solicitud.dni(),
                celular,
                CodigoRol.ROLE_APODERADO));

        UUID apoderadoId = registroRepository.crearPerfil(cuenta.id());

        registroRepository.registrarAuditoria(
                responsableId,
                apoderadoId);

        return new ApoderadoRegistradoResponse(
                apoderadoId,
                cuenta.id(),
                cuenta.nombres(),
                cuenta.apellidos(),
                celular,
                cuenta.debeCambiarPassword());
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