package com.academiacirculo.academia.estudiantes.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.estudiantes.dto.FichaEstudianteResponse;
import com.academiacirculo.academia.estudiantes.dto.RegistrarEstudianteRequest;
import com.academiacirculo.academia.estudiantes.repository.FichaEstudianteRepository;
import com.academiacirculo.academia.estudiantes.repository.RegistroEstudianteRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.usuarios.dto.CrearUsuarioRequest;
import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.service.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class RegistroEstudianteService {

    private static final ZoneId ZONA_ACADEMIA = ZoneId.of("America/Lima");

    private final UsuarioService usuarioService;
    private final RegistroEstudianteRepository registroRepository;
    private final FichaEstudianteRepository fichaRepository;
    private final VinculoApoderadoService vinculoApoderadoService;

    public RegistroEstudianteService(
            UsuarioService usuarioService,
            RegistroEstudianteRepository registroRepository,
            FichaEstudianteRepository fichaRepository,
            VinculoApoderadoService vinculoApoderadoService) {

        this.usuarioService = usuarioService;
        this.registroRepository = registroRepository;
        this.fichaRepository = fichaRepository;
        this.vinculoApoderadoService = vinculoApoderadoService;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public FichaEstudianteResponse registrar(
            @NotNull @Valid RegistrarEstudianteRequest solicitud) {

        UUID responsableId = obtenerResponsableId();
        var ficha = solicitud.ficha();
        LocalDate hoy = LocalDate.now(ZONA_ACADEMIA);

        if (!ficha.fechaNacimiento().isBefore(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento debe ser anterior a hoy.");
        }

        boolean esMenor = ficha.fechaNacimiento()
                .isAfter(hoy.minusYears(18));

        if (esMenor && solicitud.apoderado() == null) {
            throw new ConflictoDeDatosException(
                    "Un estudiante menor de edad debe tener apoderado.");
        }

        String codigo = solicitud.codigoEstudiante()
                .toUpperCase(Locale.ROOT);

        if (registroRepository.existeCodigo(codigo)) {
            throw new ConflictoDeDatosException(
                    "El código del estudiante ya está registrado.");
        }

        String celular = ficha.celular();

        if (celular.startsWith("+51")) {
            celular = celular.substring(3);
        }

        // Identificador interno. El alumno ingresará con celular y código.
        String nombreUsuario = "alumno."
                + UUID.randomUUID().toString().replace("-", "");

        var cuenta = usuarioService.crear(new CrearUsuarioRequest(
                nombreUsuario,
                ficha.correo().strip().toLowerCase(Locale.ROOT),
                solicitud.passwordTemporal(),
                ficha.nombres(),
                ficha.apellidos(),
                null,
                null,
                celular,
                CodigoRol.ROLE_ESTUDIANTE));

        UUID estudianteId = registroRepository.crearPerfil(
                cuenta.id(),
                codigo,
                ficha);

        if (solicitud.apoderado() != null) {
            vinculoApoderadoService.vincular(
                    estudianteId,
                    solicitud.apoderado());
        }

        registroRepository.registrarAuditoria(
                responsableId,
                estudianteId);

        return fichaRepository.buscarFicha(estudianteId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo consultar la ficha recién registrada."));
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