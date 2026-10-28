package com.academiacirculo.academia.apoderados.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.apoderados.dto.ApoderadoResumenResponse;

@Repository
public class BusquedaApoderadoRepository {

    private static final String FILTRO = """
            FROM apoderados a
            JOIN usuarios u ON u.id = a.usuario_id
            WHERE (
                CONCAT_WS(' ', u.nombres, u.apellidos)
                    ILIKE ? ESCAPE '!'
                OR (
                    u.tipo_documento = 'DNI'
                    AND u.numero_documento ILIKE ? ESCAPE '!'
                )
                OR u.telefono ILIKE ? ESCAPE '!'
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public BusquedaApoderadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ApoderadoResumenResponse> listar(
            String busqueda,
            int tamanio,
            long desplazamiento) {

        String sql = """
                SELECT a.id AS apoderado_id,
                       u.nombres,
                       u.apellidos,
                       CASE
                           WHEN u.tipo_documento = 'DNI'
                           THEN u.numero_documento
                           ELSE NULL
                       END AS dni,
                       u.telefono,
                       u.estado
                """ + FILTRO + """
                ORDER BY u.apellidos, u.nombres, a.id
                LIMIT ? OFFSET ?
                """;

        String patron = crearPatron(busqueda);
        String patronCelular = crearPatronCelular(busqueda);

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> new ApoderadoResumenResponse(
                        rs.getObject("apoderado_id", UUID.class),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("dni"),
                        rs.getString("telefono"),
                        rs.getString("estado")),
                patron,
                patron,
                patronCelular,
                tamanio,
                desplazamiento);
    }

    public long contar(String busqueda) {
        String patron = crearPatron(busqueda);
        String patronCelular = crearPatronCelular(busqueda);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + FILTRO,
                Long.class,
                patron,
                patron,
                patronCelular);

        return total == null ? 0L : total;
    }

    private String crearPatron(String busqueda) {
        String texto = busqueda
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + texto + "%";
    }

    private String crearPatronCelular(String busqueda) {
        String celular = busqueda;

        if (celular.matches("^\\+519[0-9]{8}$")) {
            celular = celular.substring(3);
        }

        return crearPatron(celular);
    }
}