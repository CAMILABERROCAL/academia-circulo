CREATE TABLE tokens_qr_consumidos (
    jti_hash CHAR(64) NOT NULL,
    estudiante_id UUID NOT NULL,
    sesion_clase_id UUID NOT NULL,
    asistencia_id UUID NOT NULL,
    tipo_marcacion VARCHAR(20) NOT NULL,
    consumido_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_tokens_qr_consumidos
        PRIMARY KEY (jti_hash),

    CONSTRAINT fk_tokens_qr_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiantes (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_tokens_qr_sesion
        FOREIGN KEY (sesion_clase_id)
        REFERENCES sesiones_clase (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_tokens_qr_asistencia
        FOREIGN KEY (asistencia_id)
        REFERENCES asistencias (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_tokens_qr_hash
        CHECK (LENGTH(jti_hash) = 64),

    CONSTRAINT ck_tokens_qr_tipo
        CHECK (tipo_marcacion IN (
            'ENTRADA',
            'SALIDA'
        )),

    CONSTRAINT ck_tokens_qr_expiracion
        CHECK (expira_en >= consumido_en)
);

CREATE INDEX ix_tokens_qr_estudiante
    ON tokens_qr_consumidos (estudiante_id);

CREATE INDEX ix_tokens_qr_expiracion
    ON tokens_qr_consumidos (expira_en);


CREATE TABLE escaneos_qr (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    token_fingerprint CHAR(64) NOT NULL,
    estudiante_id UUID,
    sesion_clase_id UUID,
    asistencia_id UUID,
    escaneado_por_usuario_id UUID NOT NULL,
    resultado VARCHAR(40) NOT NULL,
    detalle_codigo VARCHAR(100),
    direccion_ip INET,
    user_agent VARCHAR(500),
    escaneado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_escaneos_qr PRIMARY KEY (id),

    CONSTRAINT fk_escaneos_qr_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiantes (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_escaneos_qr_sesion
        FOREIGN KEY (sesion_clase_id)
        REFERENCES sesiones_clase (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_escaneos_qr_asistencia
        FOREIGN KEY (asistencia_id)
        REFERENCES asistencias (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_escaneos_qr_usuario
        FOREIGN KEY (escaneado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_escaneos_qr_fingerprint
        CHECK (LENGTH(token_fingerprint) = 64),

    CONSTRAINT ck_escaneos_qr_resultado
        CHECK (resultado IN (
            'ENTRADA_REGISTRADA',
            'SALIDA_REGISTRADA',
            'TOKEN_INVALIDO',
            'TOKEN_EXPIRADO',
            'TOKEN_REUTILIZADO',
            'SIN_SESION_ACTIVA',
            'NO_MATRICULADO',
            'ASISTENCIA_COMPLETA',
            'FUERA_DE_HORARIO'
        ))
);

CREATE INDEX ix_escaneos_qr_fingerprint
    ON escaneos_qr (token_fingerprint);

CREATE INDEX ix_escaneos_qr_estudiante_fecha
    ON escaneos_qr (estudiante_id, escaneado_en);

CREATE INDEX ix_escaneos_qr_sesion
    ON escaneos_qr (sesion_clase_id);


CREATE TABLE justificaciones_asistencia (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    asistencia_id UUID NOT NULL,
    motivo VARCHAR(1000) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',

    archivo_clave VARCHAR(500),
    archivo_nombre_original VARCHAR(255),
    archivo_mime VARCHAR(100),
    archivo_tamano_bytes BIGINT,
    archivo_sha256 CHAR(64),

    solicitado_por_usuario_id UUID NOT NULL,
    revisado_por_usuario_id UUID,
    solicitado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revisado_en TIMESTAMP WITH TIME ZONE,
    observacion_revision VARCHAR(500),
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_justificaciones_asistencia PRIMARY KEY (id),

    CONSTRAINT fk_justificaciones_asistencia
        FOREIGN KEY (asistencia_id)
        REFERENCES asistencias (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_justificaciones_solicitante
        FOREIGN KEY (solicitado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_justificaciones_revisor
        FOREIGN KEY (revisado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_justificaciones_motivo_no_vacio
        CHECK (BTRIM(motivo) <> ''),

    CONSTRAINT ck_justificaciones_estado
        CHECK (estado IN (
            'PENDIENTE',
            'APROBADA',
            'RECHAZADA',
            'ANULADA'
        )),

    CONSTRAINT ck_justificaciones_archivo_tamano
        CHECK (
            archivo_tamano_bytes IS NULL
            OR archivo_tamano_bytes BETWEEN 1 AND 5242880
        ),

    CONSTRAINT ck_justificaciones_archivo_hash
        CHECK (
            archivo_sha256 IS NULL
            OR LENGTH(archivo_sha256) = 64
        ),

    CONSTRAINT ck_justificaciones_archivo_mime
        CHECK (
            archivo_mime IS NULL
            OR archivo_mime IN (
                'application/pdf',
                'image/jpeg',
                'image/png',
                'image/webp'
            )
        ),

    CONSTRAINT ck_justificaciones_version
        CHECK (version >= 0)
);

CREATE INDEX ix_justificaciones_asistencia
    ON justificaciones_asistencia (asistencia_id);

CREATE INDEX ix_justificaciones_estado
    ON justificaciones_asistencia (estado);

CREATE UNIQUE INDEX ux_justificacion_activa
    ON justificaciones_asistencia (asistencia_id)
    WHERE estado IN ('PENDIENTE', 'APROBADA');