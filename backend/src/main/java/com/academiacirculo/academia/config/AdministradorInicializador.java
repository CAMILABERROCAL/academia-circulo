package com.academiacirculo.academia.config;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.academiacirculo.academia.usuarios.dto.CrearUsuarioRequest;
import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;
import com.academiacirculo.academia.usuarios.service.UsuarioService;

@Component
public class AdministradorInicializador implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdministradorInicializador.class);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Value("${BOOTSTRAP_ADMIN_ENABLED:false}")
    private boolean habilitado;

    @Value("${BOOTSTRAP_ADMIN_USERNAME:}")
    private String nombreUsuario;

    @Value("${BOOTSTRAP_ADMIN_EMAIL:}")
    private String correo;

    @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
    private String password;

    @Value("${BOOTSTRAP_ADMIN_NAMES:}")
    private String nombres;

    @Value("${BOOTSTRAP_ADMIN_SURNAMES:}")
    private String apellidos;

    public AdministradorInicializador(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!habilitado) {
            return;
        }

        if (usuarioRepository.existsByRolesCodigo(
                CodigoRol.ROLE_ADMIN)) {
            logger.info(
                    "La creación del administrador inicial fue omitida "
                            + "porque ya existe un administrador.");
            return;
        }

        validarConfiguracion();

        CrearUsuarioRequest solicitud = new CrearUsuarioRequest(
                nombreUsuario.trim(),
                correo.trim(),
                password,
                nombres.trim(),
                apellidos.trim(),
                null,
                null,
                null,
                CodigoRol.ROLE_ADMIN);

        usuarioService.crear(solicitud);

        logger.warn(
                "Administrador inicial creado. "
                        + "Desactive y elimine las variables BOOTSTRAP_ADMIN.");
    }

    private void validarConfiguracion() {
        if (esVacio(nombreUsuario)
                || esVacio(correo)
                || esVacio(password)
                || esVacio(nombres)
                || esVacio(apellidos)) {

            throw new IllegalStateException(
                    "Faltan variables para crear el administrador inicial.");
        }

        int bytesPassword = password.getBytes(StandardCharsets.UTF_8).length;

        if (password.length() < 12 || bytesPassword > 72) {
            throw new IllegalStateException(
                    "La contraseña inicial debe tener al menos 12 "
                            + "caracteres y no superar 72 bytes.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}