package com.academiacirculo.academia.estudiantes.dto;

import java.util.UUID;

public record EstudianteResumenResponse(
                UUID estudianteId,
                String codigoEstudiante,
                String nombres,
                String apellidos,
                boolean tieneMatriculaActiva) {
}