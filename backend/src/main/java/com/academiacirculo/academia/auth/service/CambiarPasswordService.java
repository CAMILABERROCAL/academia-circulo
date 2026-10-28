package com.academiacirculo.academia.auth.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.auth.dto.CambiarPasswordRequest;
import com.academiacirculo.academia.usuarios.model.EstadoUsuario;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class CambiarPasswordService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CambiarPasswordService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cambiarPassword(
            @NotNull UUID usuarioId,
            @NotNull @Valid CambiarPasswordRequest solicitud) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se pudo realizar el cambio de contraseña."));

        boolean bloqueoTemporalVigente = usuario.getBloqueadoHasta() != null
                && usuario.getBloqueadoHasta().isAfter(Instant.now());

        if (usuario.getEstado() != EstadoUsuario.ACTIVO
                || bloqueoTemporalVigente) {
            throw new IllegalArgumentException(
                    "La cuenta no está habilitada para cambiar la contraseña.");
        }

        boolean passwordActualCorrecta = passwordEncoder.matches(
                solicitud.passwordActual(),
                usuario.getPasswordHash());

        if (!passwordActualCorrecta) {
            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta.");
        }

        boolean passwordNuevaEsLaMisma = passwordEncoder.matches(
                solicitud.passwordNueva(),
                usuario.getPasswordHash());

        if (passwordNuevaEsLaMisma) {
            throw new IllegalArgumentException(
                    "La contraseña nueva debe ser diferente de la actual.");
        }

        String nuevoPasswordHash = passwordEncoder.encode(
                solicitud.passwordNueva());

        usuario.cambiarPassword(nuevoPasswordHash);

        usuarioRepository.saveAndFlush(usuario);
    }
}