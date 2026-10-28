package com.academiacirculo.academia.estudiantes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlumnoRecienteResponse(
        UUID estudianteId,
        String codigoEstudiante,
        String nombres,
        String apellidos,
        OffsetDateTime registradoEn) {
}