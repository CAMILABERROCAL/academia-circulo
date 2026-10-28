package com.academiacirculo.academia.apoderados.dto;

import java.util.UUID;

public record ApoderadoRegistradoResponse(
        UUID apoderadoId,
        UUID usuarioId,
        String nombres,
        String apellidos,
        String celular,
        boolean debeCambiarPassword) {

    @Override
    public String toString() {
        return "ApoderadoRegistradoResponse[datos protegidos]";
    }
}