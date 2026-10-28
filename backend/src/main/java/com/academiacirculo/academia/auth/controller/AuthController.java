package com.academiacirculo.academia.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.auth.dto.CambiarPasswordRequest;
import com.academiacirculo.academia.auth.dto.CsrfResponse;
import com.academiacirculo.academia.auth.dto.LoginRequest;
import com.academiacirculo.academia.auth.dto.SesionResponse;
import com.academiacirculo.academia.auth.service.AuthService;
import com.academiacirculo.academia.auth.service.CambiarPasswordService;
import com.academiacirculo.academia.auth.service.SesionesUsuarioService;
import com.academiacirculo.academia.security.service.UsuarioPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final CambiarPasswordService cambiarPasswordService;
    private final SesionesUsuarioService sesionesUsuarioService;

    private final LogoutHandler cierreSesion = new CompositeLogoutHandler(
            new SecurityContextLogoutHandler(),
            new CookieClearingLogoutHandler("SESSION", "XSRF-TOKEN"));

    public AuthController(
            AuthService authService,
            CambiarPasswordService cambiarPasswordService,
            SesionesUsuarioService sesionesUsuarioService) {

        this.authService = authService;
        this.cambiarPasswordService = cambiarPasswordService;
        this.sesionesUsuarioService = sesionesUsuarioService;
    }

    @GetMapping("/csrf")
    public CsrfResponse obtenerCsrf(CsrfToken csrfToken) {
        return CsrfResponse.desde(csrfToken);
    }

    @PostMapping("/login")
    public ResponseEntity<SesionResponse> iniciarSesion(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            SesionResponse sesion = authService.iniciarSesion(
                    loginRequest,
                    request,
                    response);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(sesion);

        } catch (AuthenticationException exception) {
            logger.warn(
                    "Autenticación rechazada: {}",
                    exception.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<SesionResponse> obtenerSesionActual(
            Authentication autenticacion) {

        SesionResponse sesion = authService.obtenerSesionActual(
                autenticacion);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sesion);
    }

    @PostMapping("/password")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequest solicitud,
            Authentication autenticacion,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        // Guarda y confirma el cambio antes de eliminar las sesiones.
        cambiarPasswordService.cambiarPassword(
                principal.getUsuarioId(),
                solicitud);

        try {
            // El nombre proviene de la sesión autenticada.
            sesionesUsuarioService.cerrarTodasLasSesiones(
                    principal.getNombreUsuario());
        } finally {
            // Limpia también el contexto y las cookies de esta petición.
            cierreSesion.logout(request, response, autenticacion);
        }

        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}