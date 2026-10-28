package com.academiacirculo.academia.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academiacirculo.academia.usuarios.model.CodigoRol;
import com.academiacirculo.academia.usuarios.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Short> {

    Optional<Rol> findByCodigo(CodigoRol codigo);

    boolean existsByCodigo(CodigoRol codigo);
}