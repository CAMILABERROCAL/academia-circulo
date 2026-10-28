package com.academiacirculo.academia.estudiantes.dto;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrarEstudianteRequest(
        @NotBlank(message = "El código del estudiante es obligatorio.") @Size(max = 30) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código admite letras, números, guion y guion bajo.") String codigoEstudiante,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña temporal es obligatoria.") @Size(min = 12, max = 64, message = "La contraseña debe tener entre 12 y 64 caracteres.") String passwordTemporal,

        @NotNull(message = "La ficha es obligatoria.") @Valid ActualizarFichaEstudianteRequest ficha,

        @Valid VincularApoderadoRequest apoderado) {

    @JsonIgnore
    @AssertTrue(message = "La contraseña no debe superar 72 bytes UTF-8.")
    public boolean isPasswordCompatibleConBCrypt() {
        return passwordTemporal == null
                || passwordTemporal.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "RegistrarEstudianteRequest[datos protegidos]";
    }
}