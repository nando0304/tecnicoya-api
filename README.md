# TécnicoYa API

API REST de la plataforma TécnicoYa, que conecta clientes con técnicos de servicios a domicilio.
Stack: Spring Boot 4.1.1, Java 17, JPA (Hibernate 7) con patrón DAO, MySQL 8 y Maven (wrapper incluido).
Librerías: Google Guava, Apache POI, Apache Commons (Lang, IO, Codec, Collections), Logback, JJWT y BCrypt.

## Documentación

| Documento | Contenido |
|---|---|
| **README.md** (este archivo) | Instalación, ejecución, comandos y guía para desarrolladores |
| [Descripción del sistema](docs/descripcion-sistema.md) | Arquitectura, módulos, modelo de datos, reglas de negocio y dependencias |
| [Referencia de la API](docs/api-rest.md) | Cada endpoint con sus parámetros, respuestas y ejemplos de uso |
| [Librerías y seguridad](docs/librerias-y-seguridad.md) | Dónde y para qué se usa cada librería Java (Guava, POI, Commons, Logback), el patrón DAO y las medidas de seguridad |
| [Javadoc](docs/apidocs/index.html) | Documentación HTML generada desde los comentarios del código (ábrala en el navegador tras clonar el repositorio) |
| [Colección de Postman](postman/TecnicoYa-API.postman_collection.json) | 116 peticiones con validaciones automáticas sobre todos los endpoints, incluidos los reportes Excel |
| [Esquema SQL](database/schema.sql) y [datos de ejemplo](database/data.sql) | Las 11 tablas y registros de prueba |

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

## 2. Configurar las credenciales

Copie `.env.example` como `.env` en la raíz del proyecto y escriba su contraseña de MySQL. El archivo `.env` está en `.gitignore` y no se sube al repositorio:

```properties
DB_USER=root
DB_PASSWORD=su_contraseña
```

También puede usar variables de entorno con los mismos nombres, que tienen prioridad sobre `.env`. Si no define nada, se usa `root`/`root`:

| Variable      | Valor por defecto                              |
|---------------|------------------------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/tecnicoya_db?...` |
| `DB_USER`     | `root`                                         |
| `DB_PASSWORD` | `root`                                         |
| `SERVER_PORT` | `8080`                                         |
| `ARCHIVOS_DIR`| `uploads` (carpeta de los archivos subidos)    |
| `JWT_SECRET`  | clave de desarrollo (cámbiela fuera de su PC; mínimo 32 caracteres) |
| `JWT_HORAS_VALIDEZ` | `8` (horas que dura el token)            |
| `LOGS_DIR`    | `logs` (carpeta de los logs de Logback)        |
| `LOGIN_MAX_INTENTOS` | `5` (intentos fallidos antes del bloqueo) |
| `LOGIN_MINUTOS_BLOQUEO` | `15` (minutos de bloqueo del login)    |
| `CORS_ORIGENES` | `http://localhost:5173,...` (orígenes del front-end permitidos) |
| `NVD_API_KEY` | opcional: acelera `dependency-check:check`     |

## 3. Levantar la API

```powershell
.\mvnw.cmd spring-boot:run          # o: mvn spring-boot:run, si tiene Maven instalado
```

En VS Code o IntelliJ también puede ejecutar `TecnicoYaApiApplication` con el botón *Run*.

Prueba rápida (inicia sesión y usa el token):

```powershell
$token = (Invoke-RestMethod -Method Post http://localhost:8080/api/auth/login -ContentType "application/json" `
  -Body '{"correo":"maria.quispe@correo.example","contrasena":"TecnicoYa2026!"}').token
curl.exe -H "Authorization: Bearer $token" http://localhost:8080/api/usuarios
```

## Comandos útiles

| Comando | Qué hace |
|---|---|
| `.\mvnw.cmd spring-boot:run` | Levanta la API en `http://localhost:8080` |
| `.\mvnw.cmd clean install` | Compila y ejecuta las pruebas unitarias (no necesitan BD) |
| `.\mvnw.cmd test` | Solo las pruebas unitarias |
| `.\mvnw.cmd javadoc:javadoc` | Regenera el Javadoc en `docs/apidocs/` |
| `.mvnw.cmd dependency-check:check` | Busca vulnerabilidades conocidas (CVE) en las librerías con OWASP Dependency-Check; reporte en `target/dependency-check-report.html` |
| `postman collection run postman/TecnicoYa-API.postman_collection.json` | Ejecuta la colección de Postman desde la terminal (con la API levantada); también sirve `npx newman run ...` |

En Linux o macOS use `./mvnw` en lugar de `.\mvnw.cmd`.

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
Devuelve `{ "token": "...", "usuario": { ... } }` o `401` si las credenciales son incorrectas o la cuenta no está `ACTIVO`.
Tras 5 intentos fallidos con el mismo correo desde la misma IP responde `429` durante 15 minutos.

Reportes Excel (solo `ADMINISTRADOR`, si no `403`): `GET /api/reportes/usuarios`, `/tecnicos`, `/servicios` y `/general`.

**Token JWT:** todas las demás rutas exigen la cabecera `Authorization: Bearer <token>`, salvo el registro
(`POST /api/usuarios`) y la descarga de archivos (`GET /api/archivos/{nombre}`). Sin token, o con uno inválido
o vencido (dura 8 horas), responden `401`. Lo valida `security/JwtInterceptor` antes de llegar al controlador.

Archivos: `POST /api/archivos` (formulario `multipart/form-data`, campo `archivo`; PDF, JPG, PNG o WEBP de hasta 5 MB)
devuelve la `url` que luego se envía en `urlArchivo` (evidencias) o `archivoEvidencia` (evidencias de pago).
`GET /api/archivos/{nombre}` descarga el archivo.

```powershell
curl.exe -H "Authorization: Bearer $token" -F "archivo=@certificado.pdf" http://localhost:8080/api/archivos
```

Respuestas: `200` (consulta/actualización), `201` con cabecera `Location` (creación), `204` (eliminación).
Los errores devuelven un JSON uniforme con mensaje en español:

```json
{ "timestamp": "...", "status": 400, "error": "Solicitud incorrecta",
  "mensaje": "La solicitud contiene datos inválidos", "ruta": "/api/usuarios",
  "errores": { "correo": "El correo no tiene un formato válido" } }
```

`400` datos inválidos o regla de negocio · `401` credenciales incorrectas o token faltante/inválido · `403` rol sin permiso · `404` recurso inexistente · `409` duplicado o registro con datos relacionados · `413` archivo demasiado grande · `429` demasiados intentos de login.

El detalle de campos, reglas y ejemplos de cada recurso está en la [referencia de la API](docs/api-rest.md).

## Estructura

El proyecto sigue el patrón **MVC**: el **Modelo** está en `model/`, el **Controlador** en `controller/` y la **Vista** es el front-end (`tecnicoya-web`), que muestra el JSON que devuelve la API.

```
src/main/java/com/tecnicoya/api
├── config/        CORS restringido al front-end, BCrypt y registro de los interceptores
├── controller/    Controlador: un controlador REST por entidad, más AuthController, ArchivoController y ReporteController
├── dao/           patrón DAO: IGenericDao e IXxxDao (contratos de acceso a datos)
│   └── impl/      implementaciones JPA con EntityManager y JPQL parametrizado (GenericDaoImpl, XxxDaoImpl)
├── security/      JWT (JwtService, JwtInterceptor), roles (@RequiereRol), límite de intentos de login,
│                  auditoría y cabeceras de seguridad
├── dto/           request (entrada validada) y response (salida)
├── model/         Modelo: entidades JPA y enums del dominio
├── exception/     excepciones de negocio y manejador global
├── service/       interfaces de servicio (documentan las reglas de negocio)
│   └── impl/      lógica de negocio
├── validation/    @SinHtml: validación contra XSS
└── util/          FechaUtil, LibroExcel (Apache POI) y Auditoria (logger de seguridad)
src/main/resources/logback-spring.xml   configuración de los logs
database/          schema.sql y data.sql
docs/              documentación técnica y Javadoc (apidocs/)
postman/           colección de pruebas de la API
```

## Guía para desarrolladores

### Convenciones

- Nombres de clases, métodos y mensajes en **español**, igual que el dominio.
- Las entidades nunca salen de la capa de servicio: los controladores reciben un `XxxRequest` y devuelven un `XxxResponse`.
- Validaciones de formato en el DTO (Bean Validation, con el mensaje en español); reglas de negocio en el servicio, lanzando `ReglaNegocioException` (400), `ConflictoException` (409) o `RecursoNoEncontradoException` (404). El `GlobalExceptionHandler` las convierte en JSON.
- Toda clase y método público lleva Javadoc. Las reglas de negocio de cada recurso se documentan en su interfaz de servicio.
- Credenciales siempre en `.env` o variables de entorno, nunca en el código ni en `application.properties`.
- Los servicios acceden a los datos solo a través de las interfaces `IXxxDao`.
- Para registrar eventos use `@Slf4j` (log general) o `Auditoria.LOG` (eventos de seguridad). Nunca registre contraseñas ni tokens.
- Los textos libres de los DTO llevan `@SinHtml`; un endpoint restringido por rol lleva `@RequiereRol`.

### Agregar un recurso nuevo

1. **Base de datos:** agregue la tabla en `database/schema.sql` (y datos de prueba en `data.sql`) y ejecute el script.
2. **Entidad:** cree la clase en `model/`, con `@Table` y `@Column` usando los nombres exactos de la tabla. Si tiene estados, cree el enum en `model/enums/`.
3. **DAO:** una interfaz `IXxxDao` en `dao/` que extienda `IGenericDao<Xxx, Long>` con sus consultas propias, y su implementación `XxxDaoImpl` en `dao/impl/` que extienda `GenericDaoImpl` y lleve `@Repository`. Use siempre parámetros nombrados (`:valor`) en el JPQL, nunca concatene valores.
4. **DTOs:** un `record` de entrada en `dto/request/` con sus validaciones y uno de salida en `dto/response/` con su método `desde(entidad)`.
5. **Servicio:** una interfaz en `service/` que extienda `CrudService<Request, Response>` y documente las reglas; su implementación en `service/impl/` con `@Transactional`.
6. **Controlador:** en `controller/`, copiando la estructura de uno existente (por ejemplo `EvidenciaController`).
7. **Pruebas:** pruebas unitarias del servicio en `src/test/java` y peticiones nuevas en la colección de Postman.
8. **Documentación:** agregue el recurso a `docs/api-rest.md` y regenere el Javadoc con `.\mvnw.cmd javadoc:javadoc`.

Al arrancar, Hibernate valida que la entidad coincida con la tabla (`ddl-auto=validate`). Si no coinciden, la aplicación no inicia e indica qué columna difiere.
