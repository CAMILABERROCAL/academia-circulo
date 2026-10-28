package com.academiacirculo.academia.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import com.academiacirculo.academia.security.filter.CambioPasswordObligatorioFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private static final String ROLE_ADMIN = "ROLE_ADMIN";
        private static final String ROLE_SECRETARIA = "ROLE_SECRETARIA";
        private static final String ROLE_PORTERO = "ROLE_PORTERO";
        private static final String ROLE_ESTUDIANTE = "ROLE_ESTUDIANTE";
        private static final String ROLE_APODERADO = "ROLE_APODERADO";

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {

                http
                                // Protección CSRF para las operaciones que modifican datos.
                                .csrf(csrf -> csrf.spa())

                                // Comunicación entre frontend y backend.
                                .cors(Customizer.withDefaults())

                                // El login se realiza mediante nuestro controller.
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)

                                // Cierre de sesión mediante POST con token CSRF válido.
                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("SESSION")
                                                .logoutSuccessHandler(
                                                                new HttpStatusReturningLogoutSuccessHandler(
                                                                                HttpStatus.NO_CONTENT)))

                                // No guardar la URL solicitada antes del login.
                                .requestCache(AbstractHttpConfigurer::disable)

                                // Crear sesiones cuando sean necesarias.
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation(fixation -> fixation.migrateSession()))

                                // Respuestas para usuarios sin sesión o sin permisos.
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                                                .accessDeniedHandler(
                                                                (request, response, exception) -> response.sendError(
                                                                                HttpStatus.FORBIDDEN.value())))

                                // Restringir el acceso si la contraseña debe cambiarse.
                                .addFilterBefore(
                                                new CambioPasswordObligatorioFilter(),
                                                AuthorizationFilter.class)

                                // Reglas de acceso por ruta.
                                .authorizeHttpRequests(authorize -> authorize

                                                // Consulta pública del estado del servidor.
                                                .requestMatchers("/actuator/health")
                                                .permitAll()

                                                // Obtener CSRF e iniciar sesión.
                                                .requestMatchers(
                                                                "/api/auth/csrf",
                                                                "/api/auth/login",
                                                                "/api/auth/login-celular")
                                                .permitAll()

                                                // Administración de cuentas de usuario.
                                                .requestMatchers(HttpMethod.POST, "/api/usuarios")
                                                .hasAuthority(ROLE_ADMIN)

                                                .requestMatchers("/api/usuarios/**")
                                                .hasAuthority(ROLE_ADMIN)

                                                // Funciones del administrador.
                                                .requestMatchers("/api/admin/**")
                                                .hasAuthority(ROLE_ADMIN)

                                                // Funciones de secretaría.
                                                .requestMatchers("/api/secretaria/**")
                                                .hasAuthority(ROLE_SECRETARIA)

                                                // Funciones del portero.
                                                .requestMatchers("/api/portero/**")
                                                .hasAuthority(ROLE_PORTERO)

                                                // Funciones del estudiante.
                                                .requestMatchers("/api/estudiante/**")
                                                .hasAuthority(ROLE_ESTUDIANTE)

                                                // Funciones del apoderado.
                                                .requestMatchers("/api/apoderado/**")
                                                .hasAuthority(ROLE_APODERADO)

                                                // Las demás rutas requieren una sesión autenticada.
                                                .requestMatchers("/api/director/**")
                                                .hasAuthority("ROLE_DIRECTOR")
                                                .anyRequest().authenticated());

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration configuration) throws Exception {

                return configuration.getAuthenticationManager();
        }
}