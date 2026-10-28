package com.academiacirculo.academia.usuarios.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Enumerated(EnumType.STRING)
    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private CodigoRol codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"), inverseJoinColumns = @JoinColumn(name = "permiso_id"))
    private Set<Permiso> permisos = new HashSet<>();

    protected Rol() {
        // Constructor requerido por JPA.
    }

    public Rol(
            CodigoRol codigo,
            String nombre,
            String descripcion) {

        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @PrePersist
    private void antesDeGuardar() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
        }
    }

    public Short getId() {
        return id;
    }

    public CodigoRol getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Set<Permiso> getPermisos() {
        return Collections.unmodifiableSet(permisos);
    }
}