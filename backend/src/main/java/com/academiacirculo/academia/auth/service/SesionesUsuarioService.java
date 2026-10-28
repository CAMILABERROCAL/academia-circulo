package com.academiacirculo.academia.auth.service;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

@Service
public class SesionesUsuarioService {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public SesionesUsuarioService(
            FindByIndexNameSessionRepository<? extends Session> sessionRepository) {

        this.sessionRepository = sessionRepository;
    }

    public void cerrarTodasLasSesiones(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio.");
        }

        var sesiones = sessionRepository.findByPrincipalName(
                nombreUsuario);

        for (String sessionId : sesiones.keySet()) {
            sessionRepository.deleteById(sessionId);
        }
    }
}