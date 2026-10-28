package com.academiacirculo.academia.apoderados.dto;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrarApoderadoRequest(
        @NotBlank(message = "Los nombres son obligatorios.") @Size(max = 120) String nombres,

        @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 120) String apellidos,

        @NotBlank(message = "El DNI es obligatorio.") @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe contener 8 dígitos.") String dni,

        @NotBlank(message = "El celular es obligatorio.") @Pattern(regexp = "^(?:\\+51)?9[0-9]{8}$", message = "Ingresa 9 dígitos de celular, opcionalmente con +51.") String celular,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña temporal es obligatoria.") @Size(min = 12, max = 64, message = "La contraseña debe tener entre 12 y 64 caracteres.") String passwordTemporal) {

    @JsonIgnore
    @AssertTrue(message = "La contraseña no debe superar 72 bytes UTF-8.")
    public boolean isPasswordCompatibleConBCrypt() {
        return passwordTemporal == null
                || passwordTemporal.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "RegistrarApoderadoRequest[datos protegidos]";
    }
}