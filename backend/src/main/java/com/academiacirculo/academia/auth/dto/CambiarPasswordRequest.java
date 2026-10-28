package com.academiacirculo.academia.auth.dto;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña actual es obligatoria.") @Size(max = 64, message = "La contraseña actual es demasiado larga.") String passwordActual,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña nueva es obligatoria.") @Size(min = 12, max = 64, message = "La contraseña nueva debe tener entre 12 y 64 caracteres.") String passwordNueva

) {

    @AssertTrue(message = "La contraseña actual no debe superar 72 bytes.")
    public boolean isPasswordActualDentroDelLimite() {
        return passwordActual == null
                || passwordActual.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @AssertTrue(message = "La contraseña nueva no debe superar 72 bytes.")
    public boolean isPasswordNuevaDentroDelLimite() {
        return passwordNueva == null
                || passwordNueva.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "CambiarPasswordRequest[passwordActual=PROTEGIDA, "
                + "passwordNueva=PROTEGIDA]";
    }
}