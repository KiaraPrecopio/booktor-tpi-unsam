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
* **Framework:** Spring Boot 3.x
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** SQL Server
* **Gestor de Dependencias:** Maven
* **Seguridad:** Spring Security

## 🏗️ Arquitectura
El proyecto sigue una arquitectura monolítica modular organizada en paquetes por responsabilidad técnica, según los requerimientos de la cátedra:
* `controllers`: Controladores REST / Spring MVC.
* `services` / `services.impl`: Interfaces de lógica de negocio y sus implementaciones.
* `repositories` / `repositories.impl`: Interfaces de acceso a datos e implementaciones personalizadas.
* `models`: Entidades de dominio (JPA).
* `dtos`: Objetos de transferencia de datos.
* `validators`: Lógica de validación personalizada.
* `exceptions`: Manejo global de errores (@ControllerAdvice).
* `configs`: Configuraciones de Spring (Security, Beans, CORS).
* `utils`: Clases utilitarias compartidas.

## 🚀 Instalación y Ejecución
1. Clonar el repositorio.
2. Configurar las credenciales de SQL Server en `src/main/resources/application.properties`.
3. Ejecutar el proyecto mediante Maven: `mvn spring-boot:run`.
