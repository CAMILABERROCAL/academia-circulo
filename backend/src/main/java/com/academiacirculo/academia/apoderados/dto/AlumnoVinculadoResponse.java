package com.academiacirculo.academia.apoderados.dto;

import java.util.UUID;

public record AlumnoVinculadoResponse(
        UUID estudianteId,
        String codigoEstudiante,
        String nombres,
        String apellidos,
        String estado,
        String parentesco) {
}