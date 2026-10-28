package com.academiacirculo.academia.usuarios.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_usuario", nullable = false, length = 80)
    private String nombreUsuario;

    @Column(name = "correo", length = 254)
    private String correo;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 120)
    private String apellidos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(name = "intentos_fallidos", nullable = false)
    private short intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

    @Column(name = "debe_cambiar_password", nullable = false)
    private boolean debeCambiarPassword;

    @Column(name = "ultimo_acceso_en")
    private Instant ultimoAccesoEn;

    @Column(name = "password_cambiado_en")
    private Instant passwordCambiadoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    protected Usuario() {
        // Constructor requerido por JPA.
    }

    public Usuario(
            String nombreUsuario,
            String correo,
            String passwordHash,
            String nombres,
            String apellidos) {

        this.nombreUsuario = Objects.requireNonNull(nombreUsuario);
        this.correo = correo;
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.nombres = Objects.requireNonNull(nombres);
        this.apellidos = Objects.requireNonNull(apellidos);
    }

    @PrePersist
    private void antesDeGuardar() {
        Instant ahora = Instant.now();

        if (estado == null) {
            estado = EstadoUsuario.ACTIVO;
        }

        creadoEn = ahora;
        actualizadoEn = ahora;
        intentosFallidos = 0;
        debeCambiarPassword = true;
    }

    @PreUpdate
    private void antesDeActualizar() {
        actualizadoEn = Instant.now();
    }

    public void establecerDocumento(
            TipoDocumento tipoDocumento,
            String numeroDocumento) {

        boolean ambosVacios = tipoDocumento == null
                && numeroDocumento == null;

        boolean ambosPresentes = tipoDocumento != null
                && numeroDocumento != null
                && !numeroDocumento.isBlank();

        if (!ambosVacios && !ambosPresentes) {
            throw new IllegalArgumentException(
                    "El tipo y número de documento deben registrarse juntos.");
        }

        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = ambosPresentes
                ? numeroDocumento.trim()
                : null;
    }

    public void establecerTelefono(String telefono) {
        this.telefono = telefono == null || telefono.isBlank()
                ? null
                : telefono.trim();
    }

    public void asignarRol(Rol rol) {
        roles.add(Objects.requireNonNull(rol));
    }

    // Recibe el hash generado por PasswordEncoder, nunca texto plano.
    public void cambiarPassword(String nuevoPasswordHash) {
        if (nuevoPasswordHash == null || nuevoPasswordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "El hash de la contraseña es obligatorio.");
        }

        this.passwordHash = nuevoPasswordHash;
        this.passwordCambiadoEn = Instant.now();
        this.debeCambiarPassword = false;
    }

    public void actualizarDatosPersonales(
            String nombres,
            String apellidos,
            String correo,
            String telefono) {

        if (nombres == null || nombres.isBlank()
                || apellidos == null || apellidos.isBlank()
                || correo == null || correo.isBlank()
                || telefono == null || telefono.isBlank()) {

            throw new IllegalArgumentException(
                    "Los nombres, apellidos, correo y celular son obligatorios.");
        }

        this.nombres = nombres.strip();
        this.apellidos = apellidos.strip();
        this.correo = correo.strip().toLowerCase(java.util.Locale.ROOT);
        this.telefono = telefono.strip();
    }

    public UUID getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public short getIntentosFallidos() {
        return intentosFallidos;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public Instant getUltimoAccesoEn() {
        return ultimoAccesoEn;
    }

    public Instant getPasswordCambiadoEn() {
        return passwordCambiadoEn;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public long getVersion() {
        return version;
    }

    public Set<Rol> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}