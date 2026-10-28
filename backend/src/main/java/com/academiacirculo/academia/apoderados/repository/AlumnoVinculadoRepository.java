package com.academiacirculo.academia.apoderados.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.apoderados.dto.AlumnoVinculadoResponse;

@Repository
public class AlumnoVinculadoRepository {

    private static final String CONSULTA_BASE = """
            SELECT
                e.id AS estudiante_id,
                e.codigo_estudiante,
                u.nombres,
                u.apellidos,
                e.estado,
                ea.parentesco
            FROM apoderados a
            JOIN estudiante_apoderados ea
                ON ea.apoderado_id = a.id
            JOIN estudiantes e
                ON e.id = ea.estudiante_id
            JOIN usuarios u
                ON u.id = e.usuario_id
            WHERE a.usuario_id = ?
            """;

    private static final RowMapper<AlumnoVinculadoResponse> MAPEADOR = (rs, fila) -> new AlumnoVinculadoResponse(
            rs.getObject("estudiante_id", UUID.class),
            rs.getString("codigo_estudiante"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("estado"),
            rs.getString("parentesco"));

    private final JdbcTemplate jdbcTemplate;

    public AlumnoVinculadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AlumnoVinculadoResponse> buscarPorUsuarioApoderado(
            UUID usuarioApoderadoId) {

        String sql = CONSULTA_BASE
                + " ORDER BY u.apellidos, u.nombres, e.id";

        return jdbcTemplate.query(
                sql,
                MAPEADOR,
                usuarioApoderadoId);
    }

    public Optional<AlumnoVinculadoResponse> buscarHijoPorId(
            UUID usuarioApoderadoId,
            UUID estudianteId) {

        String sql = CONSULTA_BASE
                + " AND e.id = ?";

        List<AlumnoVinculadoResponse> resultados = jdbcTemplate.query(
                sql,
                MAPEADOR,
                usuarioApoderadoId,
                estudianteId);

        return resultados.stream().findFirst();
    }
}