package com.academiacirculo.academia.security.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.academiacirculo.academia.security.service.UsuarioPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CambioPasswordObligatorioFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication autenticacion = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean requiereCambio = autenticacion != null
                && autenticacion.isAuthenticated()
                && autenticacion.getPrincipal() instanceof UsuarioPrincipal principal
                && principal.isDebeCambiarPassword();

        if (requiereCambio && !esOperacionPermitida(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");

            response.getWriter().write("""
                    {
                        "codigo": "CAMBIO_PASSWORD_OBLIGATORIO",
                        "mensaje": "Debes cambiar tu contraseña antes de continuar."
                    }
                    """);

            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean esOperacionPermitida(HttpServletRequest request) {
        String metodo = request.getMethod();
        String ruta = request.getServletPath();

        if ("GET".equals(metodo)) {
            return "/api/auth/csrf".equals(ruta)
                    || "/api/auth/me".equals(ruta)
                    || "/actuator/health".equals(ruta);
        }

        if ("POST".equals(metodo)) {
            return "/api/auth/password".equals(ruta)
                    || "/api/auth/logout".equals(ruta);
        }

        return false;
    }
}