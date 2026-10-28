package com.academiacirculo.academia.security.service;

import java.io.Serial;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.academiacirculo.academia.usuarios.model.EstadoUsuario;
import com.academiacirculo.academia.usuarios.model.Permiso;
import com.academiacirculo.academia.usuarios.model.Rol;
import com.academiacirculo.academia.usuarios.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

public final class UsuarioPrincipal
        implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID usuarioId;
    private final String nombreUsuario;

    private transient String passwordHash;

    private final String nombres;
    private final String apellidos;
    private final EstadoUsuario estado;
    private final Instant bloqueadoHasta;
    private final boolean debeCambiarPassword;
    private final List<GrantedAuthority> authorities;

    private UsuarioPrincipal(
            UUID usuarioId,
            String nombreUsuario,
            String passwordHash,
            String nombres,
            String apellidos,
            EstadoUsuario estado,
            Instant bloqueadoHasta,
            boolean debeCambiarPassword,
            List<GrantedAuthority> authorities) {

        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.bloqueadoHasta = bloqueadoHasta;
        this.debeCambiarPassword = debeCambiarPassword;
        this.authorities = List.copyOf(authorities);
    }

    public static UsuarioPrincipal desde(Usuario usuario) {
        return desde(usuario, List.of());
    }

    public static UsuarioPrincipal desde(
            Usuario usuario,
            Collection<String> permisosIndividuales) {

        Set<String> codigos = new TreeSet<>();

        // Roles y permisos asociados a esos roles.
        for (Rol rol : usuario.getRoles()) {
            codigos.add(rol.getCodigo().name());

            for (Permiso permiso : rol.getPermisos()) {
                codigos.add(permiso.getCodigo().name());
            }
        }

        // Permisos asignados directamente al usuario.
        codigos.addAll(permisosIndividuales);

        List<GrantedAuthority> authorities = codigos.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getPasswordHash(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getEstado(),
                usuario.getBloqueadoHasta(),
                usuario.isDebeCambiarPassword(),
                authorities);
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        boolean estadoPermiteAcceso = estado != EstadoUsuario.BLOQUEADO;

        boolean bloqueoTemporalFinalizado = bloqueadoHasta == null
                || !bloqueadoHasta.isAfter(Instant.now());

        return estadoPermiteAcceso && bloqueoTemporalFinalizado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return estado == EstadoUsuario.ACTIVO;
    }

    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}