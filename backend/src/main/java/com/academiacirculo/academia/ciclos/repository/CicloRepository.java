package com.academiacirculo.academia.ciclos.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.ciclos.dto.CicloResponse;

@Repository
public class CicloRepository {

    private final JdbcTemplate jdbcTemplate;

    public CicloRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CicloResponse> listar(
            int tamanio,
            long desplazamiento) {

        String sql = """
                SELECT id,
                       codigo,
                       nombre,
                       fecha_inicio,
                       fecha_fin,
                       duracion_meses,
                       estado
                FROM ciclos
                ORDER BY fecha_inicio DESC, codigo, id
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new CicloResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getObject("fecha_inicio", LocalDate.class),
                        rs.getObject("fecha_fin", LocalDate.class),
                        rs.getInt("duracion_meses"),
                        rs.getString("estado")),
                tamanio,
                desplazamiento);
    }
}