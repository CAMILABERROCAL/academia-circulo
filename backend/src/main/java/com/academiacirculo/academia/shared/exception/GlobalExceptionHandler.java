package com.academiacirculo.academia.shared.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ConflictoDeDatosException.class)
        public ResponseEntity<ApiError> manejarConflicto(
                        ConflictoDeDatosException exception,
                        HttpServletRequest request) {

                return construirRespuesta(
                                HttpStatus.CONFLICT,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(RecursoNoEncontradoException.class)
        public ResponseEntity<ApiError> manejarNoEncontrado(
                        RecursoNoEncontradoException exception,
                        HttpServletRequest request) {

                return construirRespuesta(
                                HttpStatus.NOT_FOUND,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> manejarValidacion(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                Map<String, String> detalles = new LinkedHashMap<>();

                for (FieldError error : exception.getBindingResult().getFieldErrors()) {

                        detalles.putIfAbsent(
                                        error.getField(),
                                        error.getDefaultMessage() == null
                                                        ? "Valor no válido."
                                                        : error.getDefaultMessage());
                }

                exception.getBindingResult()
                                .getGlobalErrors()
                                .forEach(error -> detalles.putIfAbsent(
                                                error.getObjectName(),
                                                error.getDefaultMessage() == null
                                                                ? "Datos no válidos."
                                                                : error.getDefaultMessage()));

                return construirRespuesta(
                                HttpStatus.BAD_REQUEST,
                                "Uno o más datos enviados no son válidos.",
                                request,
                                detalles);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> manejarJsonInvalido(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {

                return construirRespuesta(
                                HttpStatus.BAD_REQUEST,
                                "El contenido JSON no es válido o contiene valores incorrectos.",
                                request,
                                Map.of());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> manejarArgumentoInvalido(
                        IllegalArgumentException exception,
                        HttpServletRequest request) {

                return construirRespuesta(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> manejarIntegridad(
                        DataIntegrityViolationException exception,
                        HttpServletRequest request) {

                String incidente = UUID.randomUUID().toString();

                LOGGER.warn(
                                "Conflicto de integridad de datos. Incidente: {}",
                                incidente);

                return construirRespuesta(
                                HttpStatus.CONFLICT,
                                "La operación entra en conflicto con datos existentes.",
                                request,
                                Map.of("incidente", incidente));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> manejarAccesoDenegado(
                        AccessDeniedException exception,
                        HttpServletRequest request) {

                return construirRespuesta(
                                HttpStatus.FORBIDDEN,
                                "No tienes permiso para realizar esta operación.",
                                request,
                                Map.of());
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiError> manejarTipoParametroInvalido(
                        MethodArgumentTypeMismatchException exception,
                        HttpServletRequest request) {

                return construirRespuesta(
                                HttpStatus.BAD_REQUEST,
                                "Un parámetro de la petición tiene un formato inválido.",
                                request,
                                Map.of("parametro", exception.getName()));
        }

        private ResponseEntity<ApiError> construirRespuesta(
                        HttpStatus estado,
                        String mensaje,
                        HttpServletRequest request,
                        Map<String, String> detalles) {

                ApiError error = new ApiError(
                                Instant.now(),
                                estado.value(),
                                estado.getReasonPhrase(),
                                mensaje,
                                request.getRequestURI(),
                                detalles);

                return ResponseEntity.status(estado).body(error);
        }
}