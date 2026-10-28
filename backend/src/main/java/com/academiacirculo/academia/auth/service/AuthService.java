package com.academiacirculo.academia.auth.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import com.academiacirculo.academia.auth.dto.LoginCelularRequest;
import com.academiacirculo.academia.auth.dto.LoginRequest;
import com.academiacirculo.academia.auth.dto.SesionResponse;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;
import com.academiacirculo.academia.usuarios.model.CodigoPermiso;
import com.academiacirculo.academia.usuarios.repository.AccesoCelularRepository;
import com.academiacirculo.academia.usuarios.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

        private static final String PREFIJO_ROL = "ROLE_";

        private final AuthenticationManager authenticationManager;
        private final SecurityContextRepository securityContextRepository;
        private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
        private final AccesoCelularRepository accesoCelularRepository;
        private final UsuarioRepository usuarioRepository;

        public AuthService(
                        AuthenticationManager authenticationManager,
                        SecurityContextRepository securityContextRepository,
                        SessionAuthenticationStrategy sessionAuthenticationStrategy,
                        AccesoCelularRepository accesoCelularRepository,
                        UsuarioRepository usuarioRepository) {

                this.authenticationManager = authenticationManager;
                this.securityContextRepository = securityContextRepository;
                this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
                this.accesoCelularRepository = accesoCelularRepository;
                this.usuarioRepository = usuarioRepository;
        }

        // Acceso actual, conservado durante la transición.
        public SesionResponse iniciarSesion(
                        LoginRequest loginRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) {

                return autenticarYCrearSesion(
                                loginRequest.identificador().trim(),
                                loginRequest.password(),
                                null,
                                request,
                                response);
        }

        public SesionResponse iniciarSesionPorCelular(
                        LoginCelularRequest solicitud,
                        HttpServletRequest request,
                        HttpServletResponse response) {

                try {
                        List<UUID> candidatos = accesoCelularRepository.buscarCandidatos(
                                        solicitud.tipoAcceso(),
                                        solicitud.celular(),
                                        solicitud.codigoAlumno());

                        // No elegir una cuenta arbitrariamente si existen duplicados.
                        if (candidatos.size() != 1) {
                                throw new BadCredentialsException(
                                                "Credenciales inválidas.");
                        }

                        UUID usuarioId = candidatos.getFirst();

                        String nombreUsuario = usuarioRepository.findById(usuarioId)
                                        .orElseThrow(() -> new BadCredentialsException(
                                                        "Credenciales inválidas."))
                                        .getNombreUsuario();

                        return autenticarYCrearSesion(
                                        nombreUsuario,
                                        solicitud.password(),
                                        usuarioId,
                                        request,
                                        response);

                } catch (AuthenticationException exception) {
                        SecurityContextHolder.clearContext();
                        throw exception;
                }
        }

        private SesionResponse autenticarYCrearSesion(
                        String identificador,
                        String password,
                        UUID usuarioEsperado,
                        HttpServletRequest request,
                        HttpServletResponse response) {

                try {
                        Authentication solicitudAutenticacion = UsernamePasswordAuthenticationToken.unauthenticated(
                                        identificador,
                                        password);

                        // Comprueba contraseña y estado de la cuenta.
                        Authentication autenticacion = authenticationManager.authenticate(
                                        solicitudAutenticacion);

                        if (!(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {

                                throw new BadCredentialsException(
                                                "Credenciales inválidas.");
                        }

                        // El usuario autenticado debe ser el localizado por celular.
                        if (usuarioEsperado != null
                                        && !usuarioEsperado.equals(principal.getUsuarioId())) {

                                throw new BadCredentialsException(
                                                "Credenciales inválidas.");
                        }

                        sessionAuthenticationStrategy.onAuthentication(
                                        autenticacion,
                                        request,
                                        response);

                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                        securityContext.setAuthentication(autenticacion);
                        SecurityContextHolder.setContext(securityContext);

                        securityContextRepository.saveContext(
                                        securityContext,
                                        request,
                                        response);

                        return construirRespuestaSesion(autenticacion);

                } catch (AuthenticationException exception) {
                        SecurityContextHolder.clearContext();
                        throw exception;
                }
        }

        public SesionResponse obtenerSesionActual(
                        Authentication autenticacion) {

                return construirRespuestaSesion(autenticacion);
        }

        private SesionResponse construirRespuestaSesion(
                        Authentication autenticacion) {

                if (autenticacion == null
                                || !autenticacion.isAuthenticated()
                                || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {

                        throw new IllegalStateException(
                                        "El usuario autenticado no es válido.");
                }

                List<String> roles = autenticacion.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(codigo -> codigo.startsWith(PREFIJO_ROL))
                                .distinct()
                                .sorted()
                                .toList();

                List<String> permisos = autenticacion.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(codigo -> Arrays.stream(CodigoPermiso.values())
                                                .anyMatch(permiso -> permiso.name().equals(codigo)))
                                .distinct()
                                .sorted()
                                .toList();

                return new SesionResponse(
                                principal.getUsuarioId(),
                                principal.getNombreUsuario(),
                                principal.getNombres(),
                                principal.getApellidos(),
                                roles,
                                permisos,
                                principal.isDebeCambiarPassword());
        }
}