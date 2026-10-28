package com.academiacirculo.academia.estudiantes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarFichaEstudianteRequest(

        @NotBlank(message = "Los nombres son obligatorios.") @Size(max = 120, message = "Los nombres no deben superar 120 caracteres.") String nombres,

        @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 120, message = "Los apellidos no deben superar 120 caracteres.") String apellidos,

        @NotNull(message = "La fecha de nacimiento es obligatoria.") @Past(message = "La fecha de nacimiento debe ser anterior a hoy.") LocalDate fechaNacimiento,

        @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no tiene un formato válido.") @Size(max = 254, message = "El correo no debe superar 254 caracteres.") String correo,

        @NotBlank(message = "El celular es obligatorio.") @Pattern(regexp = "^(?:\\+51)?9[0-9]{8}$", message = "Ingresa 9 dígitos de celular, opcionalmente con +51.") String celular,

        @NotBlank(message = "La dirección referencial es obligatoria.") @Size(max = 255, message = "La dirección no debe superar 255 caracteres.") String direccionReferencial,

        @NotNull(message = "Indica si el estudiante sigue un tratamiento médico.") Boolean sigueTratamientoMedico

) {

    @Override
    public String toString() {
        return "ActualizarFichaEstudianteRequest[datos protegidos]";
    }
}