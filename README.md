# Academia Círculo

Proyecto académico de gestión de alumnos, apoderados y matrículas. Avance 3, versión entregada en el ZIP del 27 de octubre de 2026.

## Estructura

- `backend/`: API Java con Spring Boot, Spring Security y migraciones Flyway.
- `frontend/`: interfaz React con TypeScript y Vite.
- `backend/src/main/resources/db/migration/`: scripts versionados de PostgreSQL.

## Requisitos

Java 25, PostgreSQL 17 y Node.js compatible con las dependencias del frontend. El backend incluye Maven Wrapper.

## Ejecución local en Windows

Crear una base PostgreSQL vacía llamada `academia_circulo_v3`. En una terminal PowerShell, dentro de `backend`, configurar:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/academia_circulo_v3'
$env:DB_USERNAME = 'postgres'
$clave = Read-Host 'Contraseña de PostgreSQL' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $clave).Password
Remove-Variable clave
.\mvnw.cmd spring-boot:run
```

Flyway aplica las migraciones pendientes al iniciar. Las credenciales deben configurarse localmente, sin incluirlas en Git.

En otra terminal, dentro de `frontend`:

```powershell
npm.cmd install
npm.cmd run dev
```

Abrir `http://localhost:5173`. El ZIP usa el puerto **8080** para el backend y el proxy de Vite. Si se cambia a 8081, también debe ajustarse el destino del proxy en `frontend/vite.config.ts`.

## Alcance del avance

Incluye autenticación, roles y permisos, registro y consulta de alumnos y apoderados, vinculación, ciclos, turnos, horarios y matrículas. QR, asistencia, notas, pagos y reportes Excel forman parte del diseño previsto y no deben considerarse flujos completos implementados.

Spring Security y el registro de Spring Boot con Logback están presentes. Apache POI se contempla como incorporación futura.

El repositorio contiene código y migraciones, no los registros de PostgreSQL de otra computadora. Compartir datos existentes requiere una copia y restauración separada de la base de datos.
