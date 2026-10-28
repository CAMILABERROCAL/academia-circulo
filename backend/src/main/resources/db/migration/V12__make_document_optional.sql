ALTER TABLE usuarios
    ALTER COLUMN tipo_documento DROP NOT NULL,
    ALTER COLUMN numero_documento DROP NOT NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_documento_completo
    CHECK (
        (
            tipo_documento IS NULL
            AND numero_documento IS NULL
        )
        OR
        (
            tipo_documento IS NOT NULL
            AND numero_documento IS NOT NULL
        )
    );

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_tipo_documento
    CHECK (
        tipo_documento IS NULL
        OR tipo_documento IN (
            'DNI',
            'CE',
            'PASAPORTE'
        )
    );