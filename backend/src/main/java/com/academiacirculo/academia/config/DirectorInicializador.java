package com.academiacirculo.academia.config;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.RolRepository;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;

@Component
public class DirectorInicializador implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DirectorInicializador.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${BOOTSTRAP_DIRECTOR_ENABLED:false}")
    private boolean habilitado;

    @Value("${BOOTSTRAP_DIRECTOR_PASSWORD:}")
    private String password;

    public DirectorInicializador(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!habilitado) {
            return;
        }

        if (usuarioRepository.existsByRolesCodigo(
                CodigoRol.ROLE_DIRECTOR)) {

            logger.info(
                    "Ya existe un director. No se creó otra cuenta.");
            return;
        }

        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(
                "director")) {

            throw new IllegalStateException(
                    "El nombre director ya está ocupado. "
                            + "No se modificó esa cuenta.");
        }

        validarPassword();

        var rol = rolRepository.findByCodigo(
                CodigoRol.ROLE_DIRECTOR)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe ROLE_DIRECTOR en la base de datos."));

        Usuario director = new Usuario(
                "director",
                null,
                passwordEncoder.encode(password),
                "Director",
                "Academia Circulo");

        director.asignarRol(rol);

        usuarioRepository.saveAndFlush(director);

        logger.info(
                "Cuenta director creada con cambio obligatorio "
                        + "de contraseña. Desactiva BOOTSTRAP_DIRECTOR_ENABLED.");
    }

    private void validarPassword() {
        if (password == null
                || password.isBlank()
                || password.length() < 12
                || password.length() > 64
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalStateException(
                    "La contraseña temporal debe tener entre 12 y 64 "
                            + "caracteres y no superar 72 bytes UTF-8.");
        }
    }
}