package com.academiacirculo.academia.usuarios.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.shared.exception.ConflictoDeDatosException;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;
import com.academiacirculo.academia.usuarios.dto.CrearUsuarioRequest;
import com.academiacirculo.academia.usuarios.dto.UsuarioResponse;
import com.academiacirculo.academia.usuarios.model.Rol;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.RolRepository;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;
import com.academiacirculo.academia.usuarios.model.CodigoRol;

@Service
public class UsuarioService {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PasswordEncoder passwordEncoder;

        public UsuarioService(
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        PasswordEncoder passwordEncoder) {

                this.usuarioRepository = usuarioRepository;
                this.rolRepository = rolRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public UsuarioResponse crear(CrearUsuarioRequest request) {
                if (request.rol() == CodigoRol.ROLE_DIRECTOR) {
                        throw new IllegalArgumentException(
                                        "El rol Director no puede asignarse desde la creación general de usuarios.");
                }

                String nombreUsuario = request.nombreUsuario()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                String correo = normalizarCorreo(request.correo());

                String numeroDocumento = normalizarDocumento(
                                request.numeroDocumento());

                validarDuplicados(
                                nombreUsuario,
                                correo,
                                request.tipoDocumento(),
                                numeroDocumento);

                Rol rol = rolRepository.findByCodigo(request.rol())
                                .orElseThrow(() -> new RecursoNoEncontradoException(
                                                "El rol solicitado no existe."));

                String passwordHash = passwordEncoder.encode(
                                request.password());

                Usuario usuario = new Usuario(
                                nombreUsuario,
                                correo,
                                passwordHash,
                                request.nombres().trim(),
                                request.apellidos().trim());

                usuario.establecerDocumento(
                                request.tipoDocumento(),
                                numeroDocumento);

                usuario.establecerTelefono(request.telefono());
                usuario.asignarRol(rol);

                Usuario usuarioGuardado = usuarioRepository.saveAndFlush(
                                usuario);

                return UsuarioResponse.desde(usuarioGuardado);
        }

        private void validarDuplicados(
                        String nombreUsuario,
                        String correo,
                        com.academiacirculo.academia.usuarios.model.TipoDocumento tipo,
                        String numeroDocumento) {

                if (usuarioRepository.existsByNombreUsuarioIgnoreCase(
                                nombreUsuario)) {

                        throw new ConflictoDeDatosException(
                                        "El nombre de usuario ya está registrado.");
                }

                if (correo != null
                                && usuarioRepository.existsByCorreoIgnoreCase(correo)) {

                        throw new ConflictoDeDatosException(
                                        "El correo ya está registrado.");
                }

                if (tipo != null
                                && numeroDocumento != null
                                && usuarioRepository
                                                .existsByTipoDocumentoAndNumeroDocumento(
                                                                tipo,
                                                                numeroDocumento)) {

                        throw new ConflictoDeDatosException(
                                        "El documento ya está registrado.");
                }
        }

        private String normalizarCorreo(String correo) {
                if (correo == null || correo.isBlank()) {
                        return null;
                }

                return correo.trim().toLowerCase(Locale.ROOT);
        }

        private String normalizarDocumento(String numeroDocumento) {
                if (numeroDocumento == null || numeroDocumento.isBlank()) {
                        return null;
                }

                return numeroDocumento
                                .trim()
                                .toUpperCase(Locale.ROOT);
        }
}