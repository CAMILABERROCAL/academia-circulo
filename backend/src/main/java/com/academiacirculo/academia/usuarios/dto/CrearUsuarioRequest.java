package com.academiacirculo.academia.usuarios.dto;

import java.nio.charset.StandardCharsets;

import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(

        @NotBlank(message = "El nombre de usuario es obligatorio.") @Size(min = 4, max = 80) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "El usuario solo admite letras, números, punto, guion y guion bajo.") String nombreUsuario,

        @Email(message = "El correo no tiene un formato válido.") @Size(max = 254) String correo,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @NotBlank(message = "La contraseña es obligatoria.") @Size(min = 12, max = 64, message = "La contraseña debe tener entre 12 y 64 caracteres.") String password,

        @NotBlank(message = "Los nombres son obligatorios.") @Size(max = 120) String nombres,

        @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 120) String apellidos,

        TipoDocumento tipoDocumento,

        @Size(max = 30) String numeroDocumento,

        @Pattern(regexp = "^[0-9+() -]{7,30}$", message = "El teléfono no tiene un formato válido.") String telefono,

        @NotNull(message = "El rol es obligatorio.") CodigoRol rol

) {

    @JsonIgnore
    @AssertTrue(message = "El tipo y el número de documento deben enviarse juntos.")
    public boolean isDocumentoValido() {
        boolean tipoVacio = tipoDocumento == null;
        boolean numeroVacio = numeroDocumento == null
                || numeroDocumento.isBlank();

        return tipoVacio == numeroVacio;
    }

    @JsonIgnore
    @AssertTrue(message = "La contraseña supera el límite seguro de BCrypt.")
    public boolean isPasswordCompatibleConBCrypt() {
        return password == null
                || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "CrearUsuarioRequest[datos protegidos]";
    }
}