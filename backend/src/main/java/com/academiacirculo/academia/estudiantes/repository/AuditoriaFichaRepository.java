package com.academiacirculo.academia.estudiantes.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.estudiantes.dto.HistorialFichaResponse;

@Repository
public class AuditoriaFichaRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditoriaFichaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarActualizacion(
            UUID responsableId,
            UUID estudianteId) {

        String sql = """
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
                    'ACTUALIZAR_FICHA',
                    'ESTUDIANTE',
                    ?,
                    'EXITOSO',
                    'Actualización de ficha del estudiante.'
                )
                """;

        int filas = jdbcTemplate.update(
                sql,
                responsableId,
                estudianteId.toString());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría de la ficha.");
        }
    }

    public List<HistorialFichaResponse> consultarHistorial(
            UUID estudianteId,
            int tamanio,
            long desplazamiento) {

        String sql = """
                SELECT a.id,
                       u.nombre_usuario AS realizado_por,
                       a.accion,
                       a.ocurrido_en
                FROM auditoria_eventos a
                LEFT JOIN usuarios u ON u.id = a.usuario_id
                WHERE a.modulo = 'ESTUDIANTES'
                  AND a.entidad_tipo = 'ESTUDIANTE'
                  AND a.entidad_id = ?
                  AND a.accion = 'ACTUALIZAR_FICHA'
                  AND a.resultado = 'EXITOSO'
                ORDER BY a.ocurrido_en DESC, a.id DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new HistorialFichaResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("realizado_por"),
                        rs.getString("accion"),
                        rs.getObject("ocurrido_en", OffsetDateTime.class)),
                estudianteId.toString(),
                tamanio,
                desplazamiento);
    }
}