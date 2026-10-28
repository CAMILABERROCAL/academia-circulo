package com.academiacirculo.academia.matriculas.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class RegistroMatriculaRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegistroMatriculaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Boolean> bloquearEstudiante(UUID estudianteId) {
        String sql = """
                SELECT (
                    e.estado = 'ACTIVO'
                    AND u.estado = 'ACTIVO'
                ) AS habilitado
                FROM estudiantes e
                JOIN usuarios u ON u.id = e.usuario_id
                WHERE e.id = ?
                FOR UPDATE OF e, u
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> rs.getBoolean("habilitado"),
                estudianteId)
                .stream()
                .findFirst();
    }

    public Optional<String> bloquearCiclo(UUID cicloId) {
        String sql = """
                SELECT estado
                FROM ciclos
                WHERE id = ?
                FOR UPDATE
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> rs.getString("estado"),
                cicloId)
                .stream()
                .findFirst();
    }

    public Optional<String> bloquearTurno(
            UUID turnoId,
            UUID cicloId) {

        String sql = """
                SELECT estado
                FROM turnos_ciclo
                WHERE id = ?
                  AND ciclo_id = ?
                FOR UPDATE
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> rs.getString("estado"),
                turnoId,
                cicloId)
                .stream()
                .findFirst();
    }

    public boolean tieneHorarios(UUID turnoId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM horarios_turno
                    WHERE turno_id = ?
                )
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        turnoId));
    }

    public boolean tieneMatriculaActiva(UUID estudianteId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM matriculas
                    WHERE estudiante_id = ?
                      AND estado = 'ACTIVA'
                )
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        estudianteId));
    }

    public boolean existeMatriculaEnCiclo(
            UUID estudianteId,
            UUID cicloId) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM matriculas
                    WHERE estudiante_id = ?
                      AND ciclo_id = ?
                )
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        estudianteId,
                        cicloId));
    }

    public UUID registrar(
            String codigo,
            UUID estudianteId,
            UUID cicloId,
            UUID turnoId,
            LocalDate fechaMatricula) {

        String sql = """
                INSERT INTO matriculas (
                    codigo,
                    estudiante_id,
                    ciclo_id,
                    turno_id,
                    fecha_matricula,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, 'ACTIVA')
                RETURNING id
                """;

        UUID matriculaId = jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                codigo,
                estudianteId,
                cicloId,
                turnoId,
                Date.valueOf(fechaMatricula));

        if (matriculaId == null) {
            throw new IllegalStateException(
                    "No se pudo registrar la matrícula.");
        }

        return matriculaId;
    }

    public void registrarAuditoria(
            UUID responsableId,
            UUID matriculaId,
            UUID estudianteId,
            UUID cicloId,
            UUID turnoId) {

        String sql = """
                INSERT INTO auditoria_eventos (
                    usuario_id,
                    modulo,
                    accion,
                    entidad_tipo,
                    entidad_id,
                    resultado,
                    descripcion,
                    metadata
                )
                VALUES (
                    ?,
                    'MATRICULAS',
                    'REGISTRAR_MATRICULA',
                    'MATRICULA',
                    ?,
                    'EXITOSO',
                    'Registro de matrícula del estudiante.',
                    jsonb_build_object(
                        'estudianteId', CAST(? AS text),
                        'cicloId', CAST(? AS text),
                        'turnoId', CAST(? AS text)
                    )
                )
                """;

        int filas = jdbcTemplate.update(
                sql,
                responsableId,
                matriculaId.toString(),
                estudianteId.toString(),
                cicloId.toString(),
                turnoId.toString());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría de la matrícula.");
        }
    }
}