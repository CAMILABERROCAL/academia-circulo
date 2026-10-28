CREATE TABLE auditoria_eventos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    usuario_id UUID,
    modulo VARCHAR(50) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    entidad_tipo VARCHAR(100),
    entidad_id VARCHAR(100),
    resultado VARCHAR(20) NOT NULL,
    descripcion VARCHAR(500),
    metadata JSONB NOT NULL DEFAULT '{}'::JSONB,
    direccion_ip INET,
    user_agent VARCHAR(500),
    correlation_id UUID NOT NULL DEFAULT gen_random_uuid(),
    ocurrido_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_auditoria_eventos PRIMARY KEY (id),

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE SET NULL,

    CONSTRAINT ck_auditoria_modulo_no_vacio
        CHECK (BTRIM(modulo) <> ''),

    CONSTRAINT ck_auditoria_accion_no_vacia
        CHECK (BTRIM(accion) <> ''),

    CONSTRAINT ck_auditoria_resultado
        CHECK (resultado IN (
            'EXITOSO',
            'FALLIDO',
            'DENEGADO'
        ))
);

CREATE INDEX ix_auditoria_usuario_fecha
    ON auditoria_eventos (usuario_id, ocurrido_en);

CREATE INDEX ix_auditoria_modulo_fecha
    ON auditoria_eventos (modulo, ocurrido_en);

CREATE INDEX ix_auditoria_entidad
    ON auditoria_eventos (entidad_tipo, entidad_id);

CREATE INDEX ix_auditoria_correlation
    ON auditoria_eventos (correlation_id);


CREATE TABLE intentos_autenticacion (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    identificador_hash CHAR(64) NOT NULL,
    usuario_id UUID,
    resultado VARCHAR(20) NOT NULL,
    motivo VARCHAR(50),
    direccion_ip INET,
    user_agent VARCHAR(500),
    ocurrido_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_intentos_autenticacion PRIMARY KEY (id),

    CONSTRAINT fk_intentos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE SET NULL,

    CONSTRAINT ck_intentos_identificador_hash
        CHECK (LENGTH(identificador_hash) = 64),

    CONSTRAINT ck_intentos_resultado
        CHECK (resultado IN (
            'EXITOSO',
            'FALLIDO',
            'BLOQUEADO',
            'DENEGADO'
        ))
);

CREATE INDEX ix_intentos_identificador_fecha
    ON intentos_autenticacion (
        identificador_hash,
        ocurrido_en
    );

CREATE INDEX ix_intentos_ip_fecha
    ON intentos_autenticacion (
        direccion_ip,
        ocurrido_en
    );

CREATE INDEX ix_intentos_usuario_fecha
    ON intentos_autenticacion (
        usuario_id,
        ocurrido_en
    );


CREATE TABLE tokens_recuperacion_password (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expira_en TIMESTAMP WITH TIME ZONE NOT NULL,
    consumido_en TIMESTAMP WITH TIME ZONE,
    revocado_en TIMESTAMP WITH TIME ZONE,
    intentos_verificacion SMALLINT NOT NULL DEFAULT 0,
    solicitado_desde_ip INET,
    user_agent VARCHAR(500),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tokens_recuperacion_password PRIMARY KEY (id),

    CONSTRAINT uq_tokens_recuperacion_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_tokens_recuperacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_tokens_recuperacion_hash
        CHECK (LENGTH(token_hash) = 64),

    CONSTRAINT ck_tokens_recuperacion_expiracion
        CHECK (expira_en > creado_en),

    CONSTRAINT ck_tokens_recuperacion_intentos
        CHECK (intentos_verificacion BETWEEN 0 AND 10),

    CONSTRAINT ck_tokens_recuperacion_estado
        CHECK (
            consumido_en IS NULL
            OR revocado_en IS NULL
        )
);

CREATE INDEX ix_tokens_recuperacion_usuario
    ON tokens_recuperacion_password (usuario_id);

CREATE INDEX ix_tokens_recuperacion_expiracion
    ON tokens_recuperacion_password (expira_en);


CREATE TABLE historial_passwords (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    registrado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_historial_passwords PRIMARY KEY (id),

    CONSTRAINT fk_historial_passwords_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_historial_passwords_hash_no_vacio
        CHECK (BTRIM(password_hash) <> '')
);

CREATE INDEX ix_historial_passwords_usuario_fecha
    ON historial_passwords (
        usuario_id,
        registrado_en
    );