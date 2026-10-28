package com.academiacirculo.academia.estudiantes.repository;

import java.sql.Date;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.estudiantes.dto.ActualizarFichaEstudianteRequest;

@Repository
public class RegistroEstudianteRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegistroEstudianteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeCodigo(String codigo) {
        Boolean existe = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM estudiantes
                            WHERE LOWER(codigo_estudiante) = LOWER(?)
                        )
                        """,
                Boolean.class,
                codigo);

        return Boolean.TRUE.equals(existe);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID crearPerfil(
            UUID usuarioId,
            String codigo,
            ActualizarFichaEstudianteRequest ficha) {

        UUID estudianteId = UUID.randomUUID();

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO estudiantes (
                            id,
                            usuario_id,
                            codigo_estudiante,
                            fecha_nacimiento,
                            direccion_referencial,
                            sigue_tratamiento_medico
                        )
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                estudianteId,
                usuarioId,
                codigo,
                Date.valueOf(ficha.fechaNacimiento()),
                ficha.direccionReferencial().strip(),
                ficha.sigueTratamientoMedico());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo crear el perfil del estudiante.");
        }

        return estudianteId;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarAuditoria(
            UUID responsableId,
            UUID estudianteId) {

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
                            'ESTUDIANTES',
                            'REGISTRAR_ESTUDIANTE',
                            'ESTUDIANTE',
                            ?,
                            'EXITOSO',
                            'Registro de cuenta y ficha del estudiante.'
                        )
                        """,
                responsableId,
                estudianteId.toString());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría del alumno.");
        }
    }
}