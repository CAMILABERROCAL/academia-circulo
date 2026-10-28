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

import com.academiacirculo.academia.estudiantes.dto.ActualizarFichaEstudianteRequest;
import com.academiacirculo.academia.estudiantes.dto.FichaEstudianteResponse;
import com.academiacirculo.academia.estudiantes.dto.PaginaEstudiantesResponse;
import com.academiacirculo.academia.estudiantes.repository.AuditoriaFichaRepository;
import com.academiacirculo.academia.estudiantes.repository.FichaEstudianteRepository;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;
import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import com.academiacirculo.academia.estudiantes.dto.HistorialFichaResponse;

@Service
@Validated
public class FichaEstudianteService {

        private static final ZoneId ZONA_ACADEMIA = ZoneId.of("America/Lima");

        private final FichaEstudianteRepository fichaRepository;
        private final UsuarioRepository usuarioRepository;
        private final AuditoriaFichaRepository auditoriaFichaRepository;

        public FichaEstudianteService(
                        FichaEstudianteRepository fichaRepository,
                        UsuarioRepository usuarioRepository,
                        AuditoriaFichaRepository auditoriaFichaRepository) {

                this.fichaRepository = fichaRepository;
                this.usuarioRepository = usuarioRepository;
                this.auditoriaFichaRepository = auditoriaFichaRepository;
        }

        @Transactional(readOnly = true)
        @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
        public FichaEstudianteResponse consultar(
                        @NotNull UUID estudianteId) {

                return fichaRepository.buscarFicha(estudianteId)
                                .orElseThrow(() -> new RecursoNoEncontradoException(
                                                "No se encontró el estudiante solicitado."));
        }

        @Transactional
        @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
        public String actualizar(
                        @NotNull UUID estudianteId,
                        @NotNull @Valid ActualizarFichaEstudianteRequest solicitud) {

                UUID responsableId = obtenerResponsableId();
                LocalDate hoy = LocalDate.now(ZONA_ACADEMIA);

                if (!solicitud.fechaNacimiento().isBefore(hoy)) {
                        throw new IllegalArgumentException(
                                        "La fecha de nacimiento debe ser anterior a hoy.");
                }

                UUID usuarioId = fichaRepository
                                .bloquearYBuscarUsuarioId(estudianteId)
                                .orElseThrow(() -> new RecursoNoEncontradoException(
                                                "El estudiante no existe."));

                Usuario usuario = usuarioRepository.findById(usuarioId)
                                .orElseThrow(() -> new RecursoNoEncontradoException(
                                                "La cuenta del estudiante no existe."));

                boolean tieneRolEstudiante = usuario.getRoles().stream()
                                .anyMatch(rol -> rol.getCodigo() == CodigoRol.ROLE_ESTUDIANTE);

                if (!tieneRolEstudiante) {
                        throw new ConflictoDeDatosException(
                                        "La cuenta vinculada no tiene el rol de estudiante.");
                }

                boolean esMenor = solicitud.fechaNacimiento()
                                .isAfter(hoy.minusYears(18));

                if (esMenor && !fichaRepository.tieneApoderado(estudianteId)) {
                        throw new ConflictoDeDatosException(
                                        "Debes vincular un apoderado antes de completar "
                                                        + "la ficha de un estudiante menor de edad.");
                }

                String correo = solicitud.correo()
                                .strip()
                                .toLowerCase(Locale.ROOT);

                usuarioRepository.findByCorreoIgnoreCase(correo)
                                .filter(otro -> !otro.getId().equals(usuarioId))
                                .ifPresent(otro -> {
                                        throw new ConflictoDeDatosException(
                                                        "El correo ya está registrado en otra cuenta.");
                                });

                String celular = solicitud.celular();

                // Guardar los celulares peruanos en un formato uniforme.
                if (celular.startsWith("+51")) {
                        celular = celular.substring(3);
                }

                usuario.actualizarDatosPersonales(
                                solicitud.nombres(),
                                solicitud.apellidos(),
                                correo,
                                celular);

                usuarioRepository.saveAndFlush(usuario);

                fichaRepository.actualizarDatosFicha(
                                estudianteId,
                                solicitud.fechaNacimiento(),
                                solicitud.direccionReferencial().strip(),
                                solicitud.sigueTratamientoMedico());

                // La auditoría se guarda en la misma transacción que la ficha.
                auditoriaFichaRepository.registrarActualizacion(
                                responsableId,
                                estudianteId);

                // El controlador utiliza este nombre para cerrar las sesiones.
                return usuario.getNombreUsuario();
        }

        @Transactional(readOnly = true)
        @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
        public PaginaEstudiantesResponse listar(
                        int pagina,
                        int tamanio,
                        String busqueda) {

                if (pagina < 0) {
                        throw new IllegalArgumentException(
                                        "La página no puede ser negativa.");
                }

                if (tamanio < 1 || tamanio > 100) {
                        throw new IllegalArgumentException(
                                        "El tamaño de página debe estar entre 1 y 100.");
                }

                String texto = busqueda == null ? "" : busqueda.strip();

                if (texto.length() > 120) {
                        throw new IllegalArgumentException(
                                        "La búsqueda no debe superar 120 caracteres.");
                }

                long desplazamiento = (long) pagina * tamanio;

                var estudiantes = fichaRepository.listarPagina(
                                tamanio,
                                desplazamiento,
                                texto);

                long total = fichaRepository.contarEstudiantes(texto);
                long totalPaginas = total / tamanio;

                if (total % tamanio != 0) {
                        totalPaginas++;
                }

                return new PaginaEstudiantesResponse(
                                estudiantes,
                                pagina,
                                tamanio,
                                total,
                                totalPaginas);
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

        @Transactional(readOnly = true)
        @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
        public List<HistorialFichaResponse> consultarHistorial(
                        @NotNull UUID estudianteId,
                        int pagina,
                        int tamanio) {

                if (pagina < 0) {
                        throw new IllegalArgumentException(
                                        "La página no puede ser negativa.");
                }

                if (tamanio < 1 || tamanio > 100) {
                        throw new IllegalArgumentException(
                                        "El tamaño de página debe estar entre 1 y 100.");
                }

                if (fichaRepository.buscarFicha(estudianteId).isEmpty()) {
                        throw new RecursoNoEncontradoException(
                                        "No se encontró el estudiante solicitado.");
                }

                long desplazamiento = (long) pagina * tamanio;

                return auditoriaFichaRepository.consultarHistorial(
                                estudianteId,
                                tamanio,
                                desplazamiento);
        }
}