package com.academiacirculo.academia.auth.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiacirculo.academia.auth.dto.LoginCelularRequest;
import com.academiacirculo.academia.auth.dto.SesionResponse;
import com.academiacirculo.academia.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class LoginCelularController {

    private final AuthService authService;

    public LoginCelularController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login-celular")
    public ResponseEntity<SesionResponse> iniciarSesion(
            @Valid @RequestBody LoginCelularRequest solicitud,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            SesionResponse sesion = authService.iniciarSesionPorCelular(
                    solicitud,
                    request,
                    response);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(sesion);

        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
    }
}