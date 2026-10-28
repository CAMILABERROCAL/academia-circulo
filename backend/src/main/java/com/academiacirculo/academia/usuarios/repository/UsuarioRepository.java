package com.academiacirculo.academia.usuarios.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.TipoDocumento;
import com.academiacirculo.academia.usuarios.model.Usuario;

public interface UsuarioRepository
                extends JpaRepository<Usuario, UUID> {

        Optional<Usuario> findByNombreUsuarioIgnoreCase(
                        String nombreUsuario);

        Optional<Usuario> findByCorreoIgnoreCase(
                        String correo);

        Optional<Usuario> findByTipoDocumentoAndNumeroDocumento(
                        TipoDocumento tipoDocumento,
                        String numeroDocumento);

        boolean existsByNombreUsuarioIgnoreCase(
                        String nombreUsuario);

        boolean existsByCorreoIgnoreCase(
                        String correo);

        boolean existsByTipoDocumentoAndNumeroDocumento(
                        TipoDocumento tipoDocumento,
                        String numeroDocumento);

        boolean existsByRolesCodigo(
                        CodigoRol codigo);

        @EntityGraph(attributePaths = "roles")
        @Query("""
                        SELECT DISTINCT u
                        FROM Usuario u
                        WHERE LOWER(u.nombreUsuario) = LOWER(:identificador)
                           OR (
                               u.correo IS NOT NULL
                               AND LOWER(u.correo) = LOWER(:identificador)
                           )
                        """)
        Optional<Usuario> buscarParaAutenticacion(
                        @Param("identificador") String identificador);
}