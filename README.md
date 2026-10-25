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
1. Clonar el repositorio.
2. Configurar la base de datos y las variables de entorno. *(Se completa con el issue #2.)*
3. Ejecutar el proyecto con el wrapper de Maven: `.\mvnw spring-boot:run` (Windows) o `./mvnw spring-boot:run` (Linux/macOS).

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
* Ninguna credencial se sube al repositorio: van en el `.env`, que está en el `.gitignore`.
