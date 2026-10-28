package com.academiacirculo.academia.apoderados.dto;

import java.util.UUID;

public record ApoderadoResumenResponse(
        UUID apoderadoId,
        String nombres,
        String apellidos,
        String dni,
        String celular,
        String estado) {

    @Override
    public String toString() {
        return "ApoderadoResumenResponse[datos protegidos]";
    }
}