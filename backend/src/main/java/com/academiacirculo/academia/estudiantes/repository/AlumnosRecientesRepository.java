package com.academiacirculo.academia.estudiantes.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.estudiantes.dto.AlumnoRecienteResponse;

@Repository
public class AlumnosRecientesRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlumnosRecientesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AlumnoRecienteResponse> listarUltimosCinco() {
        String sql = """
                SELECT
                    e.id AS estudiante_id,
                    e.codigo_estudiante,
                    u.nombres,
                    u.apellidos,
                    e.creado_en AS registrado_en
                FROM estudiantes e
                JOIN usuarios u ON u.id = e.usuario_id
                ORDER BY e.creado_en DESC, e.id DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new AlumnoRecienteResponse(
                        rs.getObject("estudiante_id", UUID.class),
                        rs.getString("codigo_estudiante"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getObject(
                                "registrado_en",
                                OffsetDateTime.class)));
    }
}