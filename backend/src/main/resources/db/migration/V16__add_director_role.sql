INSERT INTO roles (codigo, nombre, descripcion)
VALUES (
    'ROLE_DIRECTOR',
    'Director',
    'Autoriza y revoca permisos de edición de notas para administradores'
);

INSERT INTO permisos (codigo, nombre, descripcion)
VALUES (
    'NOTAS_PERMISOS_GESTIONAR',
    'Gestionar autorizaciones de notas',
    'Permite conceder y retirar NOTAS_EDITAR a otros administradores'
);

INSERT INTO rol_permisos (rol_id, permiso_id)
VALUES (
    (SELECT id FROM roles WHERE codigo = 'ROLE_DIRECTOR'),
    (SELECT id FROM permisos
     WHERE codigo = 'NOTAS_PERMISOS_GESTIONAR')
);