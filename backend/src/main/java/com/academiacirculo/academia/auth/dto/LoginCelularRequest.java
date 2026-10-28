package com.academiacirculo.academia.auth.dto;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginCelularRequest(

        @NotNull(message = "Selecciona el tipo de acceso.") TipoAcceso tipoAcceso,

        @NotBlank(message = "El celular es obligatorio.") @Pattern(regexp = "^(?:\\+51)?9[0-9]{8}$", message = "Ingresa 9 dígitos de celular, opcionalmente con +51.") String celular,

        @Size(max = 30, message = "El código de alumno no debe superar 30 caracteres.") String codigoAlumno,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña es obligatoria.") @Size(max = 64, message = "La contraseña no debe superar 64 caracteres.") String password

) {

    @JsonIgnore
    @AssertTrue(message = "El código de alumno es obligatorio para alumnos.")
    public boolean isCodigoAlumnoValido() {
        if (tipoAcceso == null) {
            return true;
        }

        if (tipoAcceso == TipoAcceso.ALUMNO) {
            return codigoAlumno != null && !codigoAlumno.isBlank();
        }

        return codigoAlumno == null || codigoAlumno.isBlank();
    }

    @JsonIgnore
    @AssertTrue(message = "La contraseña no debe superar 72 bytes UTF-8.")
    public boolean isPasswordDentroDelLimite() {
        return password == null
                || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "LoginCelularRequest[datos protegidos]";
    }
}