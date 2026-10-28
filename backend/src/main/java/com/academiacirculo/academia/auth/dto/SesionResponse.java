package com.academiacirculo.academia.auth.dto;

import java.util.List;
import java.util.UUID;

public record SesionResponse(
                UUID usuarioId,
                String nombreUsuario,
                String nombres,
                String apellidos,
                List<String> roles,
                List<String> permisos,
                boolean debeCambiarPassword) {
}