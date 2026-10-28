package com.academiacirculo.academia.ciclos.repository;

import java.sql.Date;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.ciclos.dto.CrearCicloRequest;

@Repository
public class RegistroCicloRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegistroCicloRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeCodigo(String codigo) {
        Boolean existe = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM ciclos
                            WHERE LOWER(codigo) = LOWER(?)
                        )
                        """,
                Boolean.class,
                codigo);

        return Boolean.TRUE.equals(existe);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID crear(String codigo, CrearCicloRequest solicitud) {
        UUID cicloId = UUID.randomUUID();

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO ciclos (
                            id,
                            codigo,
                            nombre,
                            fecha_inicio,
                            fecha_fin,
                            duracion_meses,
                            estado
                        )
                        VALUES (?, ?, ?, ?, ?, ?, 'PLANIFICADO')
                        """,
                cicloId,
                codigo,
                solicitud.nombre().strip(),
                Date.valueOf(solicitud.fechaInicio()),
                Date.valueOf(solicitud.fechaFin()),
                solicitud.duracionMeses());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo crear el ciclo.");
        }

        return cicloId;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarAuditoria(UUID responsableId, UUID cicloId) {
        int filas = jdbcTemplate.update(
                """
                        INSERT INTO auditoria_eventos (
                            usuario_id,
                            modulo,
                            accion,
                            entidad_tipo,
                            entidad_id,
                            resultado,
                            descripcion
                        )
                        VALUES (
                            ?,
                            'CICLOS',
                            'CREAR_CICLO',
                            'CICLO',
                            ?,
                            'EXITOSO',
                            'Creación de ciclo en estado PLANIFICADO.'
                        )
                        """,
                responsableId,
                cicloId.toString());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría del ciclo.");
        }
    }
}