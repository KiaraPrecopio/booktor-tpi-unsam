# Booktor - Plataforma de Tutorías DUA 📚

Plataforma de agendamiento de tutorías académicas diseñada bajo los lineamientos del Diseño Universal para el Aprendizaje (DUA), orientada a facilitar el acompañamiento educativo de manera accesible e inclusiva.

## 👥 Equipo: MDD (Mate-Driven Development)
* Kalil Yasin
* Wilfredo Bardales
* Kiara Precopio

**Institución:** Escuela de Ciencia y Tecnología (ECyT) - UNSAM

**Asignatura:** Programación Avanzada (Programación 3)

## 🛠️ Stack Tecnológico
* **Lenguaje:** Java 21
* **Framework:** Spring Boot 4.1.1
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** SQL Server
* **Migraciones:** Liquibase
* **Vistas:** Thymeleaf
* **Documentación de la API:** Swagger / OpenAPI (springdoc)
* **Seguridad:** Spring Security con JWT (OAuth2 Resource Server)
* **Pruebas:** JUnit 5, Mockito, MockMvc, Cucumber, JaCoCo y PIT
* **Documentación del código:** Javadoc
* **Gestor de Dependencias:** Maven

## 🏗️ Arquitectura
El proyecto sigue una arquitectura monolítica modular organizada en paquetes por responsabilidad técnica, según los requerimientos de la cátedra:
* `controllers`: Controladores REST / Spring MVC.
* `services` / `services.impl`: Interfaces de lógica de negocio y sus implementaciones.
* `repositories`: Interfaces de Spring Data JPA para el acceso a datos.
* `models`: Entidades de dominio (JPA).
* `dtos`: Objetos de transferencia de datos.
* `validators`: Lógica de validación personalizada.
* `exceptions`: Manejo global de errores (`@RestControllerAdvice`).
* `configs`: Configuraciones de Spring (seguridad, beans, reloj).
* `tasks`: Tareas programadas (`@Scheduled`).
* `utils`: Clases utilitarias compartidas.

## 📄 Documentación
La documentación del proyecto está en [`docs/`](docs/):
* [Resumen](docs/01-resumen.md): problema, solución y actores.
* [MVP](docs/02-mvp.md): alcance, extras e hitos.
* [Definiciones](docs/03-definiciones.md): glosario, estados y reglas de negocio.
* [Requisitos](docs/04-requisitos.md): requisitos funcionales y no funcionales.
* [Modelo de datos](docs/05-modelo-datos.md): diagrama entidad-relación.

## 🚀 Instalación y Ejecución 
#### Tomado de "Guia de instalación y configuración de SQL Server"

#### Requisitos
* Java 21.
* SQL Server con autenticación mixta (SQL Server y Windows) y TCP/IP habilitado en el puerto 1433.
* SQL Server Management Studio (SSMS).

No hace falta instalar Maven: el proyecto trae el wrapper (`mvnw`).

#### 1. Clonar el repositorio
```bash
git clone <url-del-repositorio>
cd booktor-tpi-unsam
```

#### 2. Crear la base de datos, el login y los permisos
En SSMS, abrir una conexión al servidor, presionar **New Query** y ejecutar los bloques en orden. Cada integrante elige el nombre y la contraseña de su login local.

SQL Server separa el **login**, que permite entrar al servidor, del **usuario**, que define qué se puede hacer dentro de una base concreta.

```sql
-- Login a nivel servidor
CREATE LOGIN booktor
WITH PASSWORD = '<contraseña>',
CHECK_POLICY = OFF,
CHECK_EXPIRATION = OFF;
GO

-- Base de datos
CREATE DATABASE booktor_db;
GO

-- Usuario dentro de la base, vinculado al login
USE booktor_db;
GO
CREATE USER booktor FOR LOGIN booktor;
GO

-- Permisos sobre la base
ALTER ROLE db_owner ADD MEMBER booktor;
GO
```

* `CHECK_POLICY = OFF` y `CHECK_EXPIRATION = OFF` permiten una contraseña simple que no caduca. Solo son aceptables en una base local de desarrollo.
* Sin `CREATE USER`, el login entra al servidor pero no puede abrir `booktor_db`.
* `db_owner` hace falta porque Liquibase crea y modifica las tablas. En producción se darían solo los permisos necesarios.

Para comprobar que el login está habilitado:
```sql
SELECT name, is_disabled, default_database_name
FROM sys.sql_logins
WHERE name = 'booktor';
```
`is_disabled` tiene que ser `0`. Que la base por defecto sea `master` es normal: la base se indica en la URL de conexión.

#### 3. Configurar las variables de entorno
Las credenciales no se guardan en el repositorio. Copiar `.env.example` como `.env` en la raíz del proyecto y completar `DB_USERNAME` y `DB_PASSWORD` con el login del paso anterior. El resto ya trae valores de prueba:
```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=booktor_db;encrypt=false;trustServerCertificate=true
DB_USERNAME=booktor
DB_PASSWORD=<contraseña>
ADMIN_EMAIL=admin@booktor.com
ADMIN_PASSWORD_HASH=$2a$12$Pyo.NCNZ/GrP32S6uRQONOXQoIy/p/iAqgboS9nHMnJv44R98RT7O
JWT_SECRET=G88roQ7xj0l9mCIHdqaUu6lfoVOQcdwgbj1SF9X4k2tjqyql
```
`DB_URL` va en una sola línea, sin espacios. El `.env` está en el `.gitignore`.

`ADMIN_EMAIL` y `ADMIN_PASSWORD_HASH` son los datos del administrador inicial, que Liquibase crea la primera vez que arranca la aplicación. La contraseña no se guarda en texto plano, sino su hash BCrypt. El hash del ejemplo corresponde a la contraseña de prueba `Admin1234`, así que el administrador entra con `admin@booktor.com` / `Admin1234`. Es una credencial de prueba: en un entorno real se reemplaza por el hash de una contraseña propia.

`JWT_SECRET` es la clave con la que se firman los tokens de inicio de sesión, y tiene que tener al menos 32 caracteres: sin esa variable la aplicación no arranca. El valor del ejemplo sirve para probar el proyecto; en un entorno real se reemplaza por un texto aleatorio propio. Si se cambia, los tokens emitidos con la clave anterior dejan de ser válidos.

#### 4. Ejecutar
```bash
.\mvnw spring-boot:run    # Windows
./mvnw spring-boot:run    # Linux/macOS
```
Al arrancar, Liquibase aplica los changesets pendientes. Las tablas no las crea Hibernate: `ddl-auto=validate` solo verifica que coincidan con las entidades.

La primera vez, en `booktor_db > Tables` tienen que aparecer `DATABASECHANGELOG` y `DATABASECHANGELOGLOCK`.

Para correr las pruebas: `.\mvnw test`. Para el reporte de cobertura de JaCoCo: `.\mvnw verify`, que lo genera en `target/site/jacoco/index.html`.

Las pruebas usan objetos simulados (Mockito) y no tocan la base, salvo `BooktorApplicationTests`, que levanta la aplicación completa: necesita SQL Server y el `.env` configurados como en los pasos 2 y 3.

| Herramienta | Qué hace | Comando | Resultado |
|---|---|---|---|
| Cucumber | Ejecuta los escenarios de `src/test/resources/features` escritos en Gherkin (Given / When / Then). Corre junto con el resto de las pruebas. | `.\mvnw test` | En la consola y en `target/cucumber-reports/cucumber.html` |
| PIT | Mutation testing: modifica a propósito el código de `services.impl` y verifica que algún test falle. No corre en el build normal porque tarda. | `.\mvnw test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` |
| Javadoc | Genera la documentación HTML del código. Todas las clases tienen Javadoc; las interfaces de `services` describen cada operación con sus reglas y excepciones. | `.\mvnw javadoc:javadoc` | `target/reports/apidocs/index.html` |

#### 5. Datos de demo (opcional)
Para cargar datos de ejemplo (materias, adaptaciones, tutores, estudiantes, turnos y reservas), agregar en el `.env`:
```properties
LIQUIBASE_CONTEXTS=demo
```
Al arrancar, Liquibase aplica el changeset `007-datos-demo`. Sin esa línea, la base queda solo con el administrador inicial. Los turnos se generan a partir del día en que se carga la base, así que siempre son futuros.

Todos los usuarios de demo tienen la contraseña de prueba `Test1234` (no es una credencial real):

| Rol | Email | Datos |
|---|---|---|
| Tutor | `tutor@booktor.com` | Lucía Fernández. Turnos de 60 min. Ofrece Material visual y Lectura fácil. |
| Tutor | `tutor2@booktor.com` | Martín Gómez. Turnos de 45 min. Ofrece solo Subtítulos. |
| Tutor | `tutor3@booktor.com` | Sofía Ruiz. Turnos de 30 min. Ofrece Material visual y Tiempo extendido. |
| Estudiante | `estudiante@booktor.com` | Juan Pérez. Tiene reservas activas con Lucía y con Martín. |
| Estudiante | `estudiante2@booktor.com` | Tomás Díaz. Tiene una reserva activa con Sofía y una cancelada con Lucía. |

Para volver a cargar los datos de demo hay que vaciar la base, porque Liquibase aplica cada changeset una sola vez.

#### 6. Iniciar sesión (JWT)
La aplicación no guarda sesiones en el servidor: cada pedido trae un token JWT firmado que indica el email y el rol del usuario. El token vence a la hora.

**Desde el navegador:** entrar a `http://localhost:8080/login`. Al iniciar sesión, el token se guarda en una cookie `HttpOnly` y `SameSite=Strict`, y cada usuario va al menú de su rol. "Cerrar sesión" borra la cookie.

**Desde la API (Postman o Swagger):**
1. Pedir el token:
   ```http
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json

   { "email": "tutor@booktor.com", "password": "Test1234" }
   ```
   Respuesta: `{ "token": "eyJ...", "tipo": "Bearer" }`.
2. Mandar el token en el resto de los pedidos:
   ```http
   GET http://localhost:8080/api/turnos/mis-turnos
   Authorization: Bearer eyJ...
   ```
3. En Swagger (`http://localhost:8080/swagger-ui.html`), presionar **Authorize** y pegar el token, sin la palabra `Bearer`.

| Caso | Respuesta |
|---|---|
| Email o contraseña incorrectos | 401 |
| Sin token, o con un token vencido o alterado | 401 en la API; el navegador vuelve al login |
| Token válido, pero de un rol que no corresponde | 403 |

#### 7. Probar la API con Postman
En [`docs/postman/`](docs/postman/) hay dos colecciones y un entorno:

| Archivo | Para qué |
|---|---|
| `booktor.postman_collection.json` | Recorre toda la API con los tres roles: 55 pedidos, cada uno verifica su código de respuesta. |
| `booktor-demo.postman_collection.json` | Tres pedidos para mostrar JWT con la tutora Lucía: login (200), sus turnos (200) y el ABML de tutores (403). Necesita los datos de demo. |
| `booktor.postman_environment.json` | Entorno `Booktor local`: `baseUrl`, credenciales del administrador y la contraseña de prueba. |

1. En Postman, **Import** y elegir los tres archivos.
2. Seleccionar el entorno **Booktor local**. Ya trae el administrador de prueba (`admin@booktor.com` / `Admin1234`); si en el `.env` se usa otro, cambiar `admin_email` y `admin_password`.
3. Con la aplicación levantada, abrir la colección y presionar **Run**.

Cada pedido de login guarda el token en una variable de la colección, y el resto lo manda como `Authorization: Bearer`.

La colección completa crea sus propios datos (nombres con "(prueba ...)" y emails `@postman.test`), así que se puede correr varias veces sobre la misma base. Las carpetas van en orden y conviene correrlas completas:

| Carpeta | Qué prueba |
|---|---|
| 0. Autenticación | Login correcto, contraseña incorrecta (401), pedido sin token y con token alterado (401). |
| 1. Administrador | ABML de materias, adaptaciones, tutores y estudiantes con sus validaciones (400, 404, 409), reservas activas y un 403. |
| 2. Tutor | Generar un bloque de turnos, bloque superpuesto (409), virtual sin link (400), sus turnos y un 403. |
| 3. Estudiante | Buscar con filtros, reservar con objetivo, reservar dos veces el mismo turno (409), cancelar y volver a reservar, y un 403. |
| 4. Cancelaciones y bajas de turnos | Baja de un turno reservado (409), cancelación por el tutor y por el administrador, y baja de turnos libres. |
| 5. Administrador: bajas | Baja de los datos creados por la colección. |

También se puede correr desde la consola con [Postman CLI](https://learning.postman.com/docs/postman-cli/postman-cli-overview/):
```bash
postman collection run docs/postman/booktor.postman_collection.json -e docs/postman/booktor.postman_environment.json
```

## 🤝 Convenciones del equipo

### Ramas
* `main`: versión estable, lista para entregar.
* `develop`: integración del trabajo del equipo. Todas las ramas salen de acá y vuelven acá.
* `feature/<n>-<descripcion>`: una rama por issue, con su número. Por ejemplo, `feature/7-abml-materias`.

### Commits
Se usa [Conventional Commits](https://www.conventionalcommits.org/es/):
* `feat:` funcionalidad nueva.
* `fix:` corrección de un error.
* `docs:` documentación.
* `test:` pruebas.
* `refactor:` cambios de código que no alteran el comportamiento.
* `chore:` configuración, dependencias y tareas de mantenimiento.

Ejemplo: `feat: agregar alta de materias`.

### Pull requests
* Un pull request por issue, contra `develop`.
* Lo revisa al menos otro integrante antes de mergear.
* La descripción incluye `Closes #<n>` para vincular el issue.
* Antes de abrirlo, el proyecto compila y las pruebas pasan (`.\mvnw test`).

### Código
* Nombres de clases, métodos y variables en español (por ejemplo, `Turno`, `reservarTurno`). Los sufijos técnicos van en inglés (`TurnoService`, `TurnoController`).
* El esquema de la base solo se cambia con un changeset nuevo de Liquibase. Un changeset ya aplicado no se edita.
* El changelog maestro (`db/changelog/db.changelog-master.xml`) está en XML y solo incluye los changesets.
* Los changesets se escriben en SQL (formato *formatted SQL* de Liquibase) dentro de `db/changelog/changes/`, en archivos numerados en orden (`001-crear-usuario-y-admin.sql`, `002-...`). Un archivo puede tener varios changesets. Cada archivo empieza con `--liquibase formatted sql`, y cada changeset con `--changeset <autor>:<id>`.
* Las tablas se nombran en singular y en minúscula, con guion bajo (`usuario`, `usuario_adaptacion`), igual que en el modelo de datos.
* Ninguna credencial se sube al repositorio: van en el `.env`, que está en el `.gitignore`.
* La hora actual se obtiene del bean Clock: LocalDateTime.now(clock), no LocalDateTime.now()
