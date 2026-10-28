package com.academiacirculo.academia.estudiantes.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.estudiantes.dto.IndicadoresAlumnosResponse;

@Repository
public class IndicadoresAlumnosRepository {

    private final JdbcTemplate jdbcTemplate;

    public IndicadoresAlumnosRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public IndicadoresAlumnosResponse consultar() {
        String sql = """
                SELECT
                    COUNT(*) AS total_alumnos,
                    COUNT(*) FILTER (
                        WHERE tiene_matricula
                    ) AS con_matricula_activa,
                    COUNT(*) FILTER (
                        WHERE NOT tiene_matricula
                    ) AS sin_matricula_activa
                FROM (
                    SELECT EXISTS (
                        SELECT 1
                        FROM matriculas m
                        WHERE m.estudiante_id = e.id
                          AND m.estado = 'ACTIVA'
                    ) AS tiene_matricula
                    FROM estudiantes e
                ) alumnos
                """;

        IndicadoresAlumnosResponse respuesta = jdbcTemplate.queryForObject(
                sql,
                (rs, fila) -> new IndicadoresAlumnosResponse(
                        rs.getLong("total_alumnos"),
                        rs.getLong("con_matricula_activa"),
                        rs.getLong("sin_matricula_activa")));

        if (respuesta == null) {
            throw new IllegalStateException(
                    "No se pudieron consultar los indicadores.");
        }

        return respuesta;
    }
}