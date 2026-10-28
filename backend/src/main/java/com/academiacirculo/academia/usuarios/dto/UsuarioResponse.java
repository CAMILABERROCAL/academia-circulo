package com.academiacirculo.academia.usuarios.dto;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.EstadoUsuario;
import com.academiacirculo.academia.usuarios.model.Rol;
import com.academiacirculo.academia.usuarios.model.TipoDocumento;
import com.academiacirculo.academia.usuarios.model.Usuario;

public record UsuarioResponse(
        UUID id,
        String nombreUsuario,
        String correo,
        String nombres,
        String apellidos,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String telefono,
        EstadoUsuario estado,
        List<CodigoRol> roles,
        boolean debeCambiarPassword,
        Instant creadoEn) {

    public static UsuarioResponse desde(Usuario usuario) {
        List<CodigoRol> codigosRol = usuario.getRoles()
                .stream()
                .map(Rol::getCodigo)
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getCorreo(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getTipoDocumento(),
                usuario.getNumeroDocumento(),
                usuario.getTelefono(),
                usuario.getEstado(),
                codigosRol,
                usuario.isDebeCambiarPassword(),
                usuario.getCreadoEn());
    }
}