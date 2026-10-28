package com.academiacirculo.academia.estudiantes.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MiPerfilEstudianteResponse(
        UUID estudianteId,
        String codigoEstudiante,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String correo,
        String celular,
        String direccionReferencial,
        String estado) {

    @Override
    public String toString() {
        return "MiPerfilEstudianteResponse[datos personales protegidos]";
    }
}