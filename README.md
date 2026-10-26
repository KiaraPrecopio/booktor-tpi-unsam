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
* **Seguridad:** Spring Security
* **Pruebas:** JUnit 5, Mockito, MockMvc y JaCoCo
* **Gestor de Dependencias:** Maven

## 🏗️ Arquitectura
El proyecto sigue una arquitectura monolítica modular organizada en paquetes por responsabilidad técnica, según los requerimientos de la cátedra:
* `controllers`: Controladores REST / Spring MVC.
* `services` / `services.impl`: Interfaces de lógica de negocio y sus implementaciones.
* `repositories` / `repositories.impl`: Interfaces de acceso a datos e implementaciones personalizadas.
* `models`: Entidades de dominio (JPA).
* `dtos`: Objetos de transferencia de datos.
* `validators`: Lógica de validación personalizada.
* `exceptions`: Manejo global de errores (`@RestControllerAdvice`).
* `configs`: Configuraciones de Spring (seguridad, beans, reloj).
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
Las credenciales no se guardan en el repositorio. Copiar `.env.example` como `.env` en la raíz del proyecto y completarlo con el login del paso anterior:
```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=booktor_db;encrypt=false;trustServerCertificate=true
DB_USERNAME=booktor
DB_PASSWORD=<contraseña>
```
`DB_URL` va en una sola línea, sin espacios. El `.env` está en el `.gitignore`.

#### 4. Ejecutar
```bash
.\mvnw spring-boot:run    # Windows
./mvnw spring-boot:run    # Linux/macOS
```
Al arrancar, Liquibase aplica los changesets pendientes. Las tablas no las crea Hibernate: `ddl-auto=validate` solo verifica que coincidan con las entidades.

La primera vez, en `booktor_db > Tables` tienen que aparecer `DATABASECHANGELOG` y `DATABASECHANGELOGLOCK`.

Para correr las pruebas: `.\mvnw test`.

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
* Los changesets se escriben en SQL (formato *formatted SQL* de Liquibase) dentro de `db/changelog/changes/`, numerados en orden: `001-crear-usuario.sql`, `002-...`. Cada archivo empieza con `--liquibase formatted sql`, y cada changeset con `--changeset <autor>:<id>`.
* Ninguna credencial se sube al repositorio: van en el `.env`, que está en el `.gitignore`.
* La hora actual se obtiene del bean Clock: LocalDateTime.now(clock), no LocalDateTime.now()
