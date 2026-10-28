package com.academiacirculo.academia.apoderados.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.apoderados.dto.AlumnoVinculadoResponse;

@Repository
public class ConsultaAlumnosApoderadoRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaAlumnosApoderadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeApoderado(UUID apoderadoId) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM apoderados WHERE id = ?)",
                Boolean.class,
                apoderadoId);

        return Boolean.TRUE.equals(existe);
    }

    public List<AlumnoVinculadoResponse> listarAlumnos(UUID apoderadoId) {
        String sql = """
                SELECT e.id AS estudiante_id,
                       e.codigo_estudiante,
                       u.nombres,
                       u.apellidos,
                       e.estado,
                       ea.parentesco
                FROM estudiante_apoderados ea
                JOIN estudiantes e ON e.id = ea.estudiante_id
                JOIN usuarios u ON u.id = e.usuario_id
                WHERE ea.apoderado_id = ?
                ORDER BY u.apellidos, u.nombres, e.id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new AlumnoVinculadoResponse(
                        rs.getObject("estudiante_id", UUID.class),
                        rs.getString("codigo_estudiante"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("estado"),
                        rs.getString("parentesco")),
                apoderadoId);
    }
}