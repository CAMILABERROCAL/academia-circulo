package com.academiacirculo.academia.estudiantes.dto;

import java.time.LocalDate;
import java.util.UUID;

public record FichaEstudianteResponse(
        UUID estudianteId,
        String codigoEstudiante,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String correo,
        String celular,
        String direccionReferencial,
        Boolean sigueTratamientoMedico,
        boolean tieneApoderado,
        String nombreApoderado,
        String celularApoderado,
        String parentescoApoderado) {

    @Override
    public String toString() {
        return "FichaEstudianteResponse[datos personales protegidos]";
    }
}