package com.academiacirculo.academia.security.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.usuarios.model.Usuario;
import com.academiacirculo.academia.usuarios.repository.UsuarioPermisoRepository;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;

@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPermisoRepository usuarioPermisoRepository;

    public UsuarioUserDetailsService(
            UsuarioRepository usuarioRepository,
            UsuarioPermisoRepository usuarioPermisoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioPermisoRepository = usuarioPermisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identificador) {

        if (identificador == null || identificador.isBlank()) {
            throw new UsernameNotFoundException(
                    "Credenciales inválidas.");
        }

        Usuario usuario = usuarioRepository
                .buscarParaAutenticacion(identificador.trim())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Credenciales inválidas."));

        List<String> permisosIndividuales = usuarioPermisoRepository.buscarCodigosPorUsuarioId(
                usuario.getId());

        return UsuarioPrincipal.desde(
                usuario,
                permisosIndividuales);
    }
}