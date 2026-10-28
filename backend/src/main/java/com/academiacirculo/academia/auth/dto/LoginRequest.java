package com.academiacirculo.academia.auth.dto;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "El usuario o correo es obligatorio.") @Size(max = 254, message = "El identificador es demasiado largo.") String identificador,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña es obligatoria.") @Size(max = 64, message = "La contraseña es demasiado larga.") String password

) {

    @AssertTrue(message = "La contraseña supera el límite permitido.")
    public boolean isPasswordDentroDelLimite() {
        return password == null
                || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "LoginRequest[identificador="
                + identificador
                + ", password=PROTEGIDA]";
    }
}