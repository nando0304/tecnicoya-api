# TécnicoYa API

API REST de la plataforma TécnicoYa, que conecta clientes con técnicos de servicios a domicilio.
Stack: Spring Boot 4.1.1, Java 17, Spring Data JPA (Hibernate 7), MySQL 8 y Maven (wrapper incluido).

## Requisitos

- JDK 17 o superior
- MySQL 8.0.16 o superior (hace falta para que se apliquen las restricciones `CHECK`)
- Maven no es necesario: `mvnw` / `mvnw.cmd` descarga Maven 3.9.16 la primera vez

## 1. Crear la base de datos

Desde la raíz del proyecto (pide la contraseña de MySQL):

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql -u root -p --default-character-set=utf8mb4 -e "source database/schema.sql"
& $mysql -u root -p --default-character-set=utf8mb4 -e "source database/data.sql"
```

También puede abrir y ejecutar ambos scripts en MySQL Workbench, en ese orden.
`schema.sql` **borra y recrea** las tablas: úselo solo en desarrollo.

Todos los usuarios de ejemplo tienen la contraseña `TecnicoYa2026!`.

## 2. Levantar la API

La conexión se configura con variables de entorno. Si no se definen, se usa `root`/`root`:

| Variable      | Valor por defecto                              |
|---------------|------------------------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/tecnicoya_db?...` |
| `DB_USER`     | `root`                                         |
| `DB_PASSWORD` | `root`                                         |
| `SERVER_PORT` | `8080`                                         |

```powershell
$env:DB_PASSWORD = "su_contraseña"
.\mvnw.cmd spring-boot:run          # o: mvn spring-boot:run, si tiene Maven instalado
```

Compilar y ejecutar las pruebas unitarias (no necesitan BD): `.\mvnw.cmd clean install`

Prueba rápida: `curl.exe http://localhost:8080/api/usuarios`

## Endpoints

Cada recurso expone `GET /`, `GET /{id}`, `POST /`, `PUT /{id}` y `DELETE /{id}`:

| Recurso               | Ruta base                     |
|-----------------------|-------------------------------|
| Usuarios              | `/api/usuarios`               |
| Técnicos              | `/api/tecnicos`               |
| Servicios             | `/api/servicios`              |
| Disponibilidades      | `/api/disponibilidades`       |
| Evidencias            | `/api/evidencias`             |
| Experiencias laborales| `/api/experiencias-laborales` |
| Calificaciones        | `/api/calificaciones`         |
| Evidencias de pago    | `/api/evidencias-pago`        |
| Planes de suscripción | `/api/planes-suscripcion`     |
| Suscripciones         | `/api/suscripciones`          |
| Pagos de suscripción  | `/api/pagos-suscripcion`      |

Inicio de sesión: `POST /api/auth/login` con `{ "correo": "...", "contrasena": "..." }`.
Devuelve el usuario (sin contraseña) o `401` si las credenciales son incorrectas o la cuenta no está `ACTIVO`.

Respuestas: `200` (consulta/actualización), `201` con cabecera `Location` (creación), `204` (eliminación).
Los errores devuelven un JSON uniforme con mensaje en español:

```json
{ "timestamp": "...", "status": 400, "error": "Solicitud incorrecta",
  "mensaje": "La solicitud contiene datos inválidos", "ruta": "/api/usuarios",
  "errores": { "correo": "El correo no tiene un formato válido" } }
```

`400` datos inválidos o regla de negocio · `401` credenciales incorrectas · `404` recurso inexistente · `409` duplicado o registro con datos relacionados.

## Estructura

```
src/main/java/com/tecnicoya/api
├── config/        CORS abierto (solo desarrollo) y BCrypt
├── controller/    un controlador REST por entidad
├── dto/           request (entrada validada) y response (salida)
├── entity/        entidades JPA y enums del dominio
├── exception/     excepciones de negocio y manejador global
├── repository/    interfaces JpaRepository
├── service/       interfaces de servicio
│   └── impl/      lógica de negocio
└── util/
database/          schema.sql y data.sql
```
