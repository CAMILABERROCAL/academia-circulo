package com.academiacirculo.academia.auth.dto;

import org.springframework.security.web.csrf.CsrfToken;

public record CsrfResponse(
        String headerName,
        String parameterName,
        String token) {

    public static CsrfResponse desde(CsrfToken csrfToken) {
        return new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken());
    }
}