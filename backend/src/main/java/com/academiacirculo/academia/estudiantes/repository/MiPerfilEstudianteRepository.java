package com.academiacirculo.academia.estudiantes.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.estudiantes.dto.MiPerfilEstudianteResponse;

@Repository
public class MiPerfilEstudianteRepository {

    private final JdbcTemplate jdbcTemplate;

    public MiPerfilEstudianteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<MiPerfilEstudianteResponse> buscarPorUsuarioId(
            UUID usuarioId) {

        String sql = """
                SELECT e.id AS estudiante_id,
                       e.codigo_estudiante,
                       u.nombres,
                       u.apellidos,
                       e.fecha_nacimiento,
                       u.correo,
                       u.telefono,
                       e.direccion_referencial,
                       e.estado
                FROM estudiantes e
                JOIN usuarios u ON u.id = e.usuario_id
                WHERE e.usuario_id = ?
                """;

        List<MiPerfilEstudianteResponse> resultados = jdbcTemplate.query(
                sql,
                (rs, fila) -> new MiPerfilEstudianteResponse(
                        rs.getObject("estudiante_id", UUID.class),
                        rs.getString("codigo_estudiante"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getObject("fecha_nacimiento", LocalDate.class),
                        rs.getString("correo"),
                        rs.getString("telefono"),
                        rs.getString("direccion_referencial"),
                        rs.getString("estado")),
                usuarioId);

        return resultados.stream().findFirst();
    }
}