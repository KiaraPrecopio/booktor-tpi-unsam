# Requisitos

Los requisitos funcionales (RF) describen **qué hace** el sistema. Los no funcionales (RNF) describen **cómo** tiene que hacerlo. Cada RF indica las reglas de negocio de [03-definiciones.md](03-definiciones.md) que lo afectan.

## Requisitos funcionales

### Acceso
| ID | Requisito | Reglas |
|---|---|---|
| **RF-01** | El sistema permite iniciar sesión a estudiantes, tutores y administradores. | — |
| **RF-02** | Cada actor accede solo a las operaciones de su rol. | — |
| **RF-03** | Al iniciar, el sistema tiene un administrador inicial cargado. No hay ABML de administradores. | — |

### Catálogos (Administrador)
| ID | Requisito | Reglas |
|---|---|---|
| **RF-04** | El administrador puede dar de alta, dar de baja, modificar y listar materias. | RN-05, RN-23, RN-26 |
| **RF-05** | El administrador puede dar de alta, dar de baja, modificar y listar adaptaciones. | RN-05, RN-23, RN-26 |

### Usuarios
| ID | Requisito | Reglas |
|---|---|---|
| **RF-06** | El administrador puede dar de alta, dar de baja, modificar y listar estudiantes, con sus adaptaciones preferidas. | RN-01 a RN-03, RN-23, RN-24 |
| **RF-07** | El administrador puede dar de alta, dar de baja, modificar y listar tutores, con las adaptaciones que ofrecen y la duración de sus turnos. | RN-01 a RN-04, RN-23 a RN-25 |

### Turnos del tutor
| ID | Requisito | Reglas |
|---|---|---|
| **RF-08** | El tutor genera turnos en bloque, indicando materia, fecha, hora desde, hora hasta y modalidad (con el link o el lugar). El sistema divide el bloque en turnos según la duración del tutor. | RN-03, RN-06 a RN-11, RN-27 |
| **RF-09** | El tutor puede dar de baja sus turnos DISPONIBLES. | RN-12, RN-23 |

### Turnos y reservas
| ID | Requisito | Reglas |
|---|---|---|
| **RF-10** | El estudiante busca materias por nombre, elige una y ve los tutores con turnos disponibles en ella, con sus adaptaciones y sus turnos (con su modalidad). Puede filtrar por adaptación y por modalidad. | RN-15, RN-18 a RN-20, RN-28 |
| **RF-11** | El estudiante puede reservar un turno disponible e indicar, de forma opcional, su objetivo para la tutoría. | RN-03, RN-13 a RN-17, RN-29 |
| **RF-12** | El estudiante, el tutor o el administrador pueden cancelar una reserva. El administrador puede listar las reservas ACTIVAS de turnos futuros para elegir cuál cancelar. | RN-21, RN-22 |
| **RF-13** | "Mis turnos": el estudiante lista sus próximas reservas ACTIVAS, con la modalidad y el link o el lugar; el tutor lista sus próximos turnos DISPONIBLES y ASIGNADOS, con el estudiante y el objetivo de cada reserva. | RN-28, RN-29 |
| **RF-14** | El sistema pasa automáticamente a FINALIZADO los turnos asignados cuyo horario ya terminó, mediante una tarea programada. | — |

### Errores
| ID | Requisito | Reglas |
|---|---|---|
| **RF-15** | Cuando el sistema rechaza una operación, informa el motivo y, si corresponde, qué registros lo provocan. | Respuesta ante un conflicto (`03`) |

## Requisitos no funcionales

Los lineamientos técnicos del TPI no se repiten acá. Esta sección lista solo lo que agrega el equipo.

### Datos
- **RNF-01.** El esquema y los datos iniciales se manejan con Liquibase. Hibernate solo valida el esquema (`ddl-auto=validate`).
- **RNF-02.** El login usa Spring Security con sesión. Las contraseñas se guardan encriptadas (hash BCrypt), nunca en texto plano, y nunca se devuelven en las respuestas.
- **RNF-03.** Ninguna credencial (base de datos, administrador inicial) se guarda en el repositorio: se leen de variables de entorno definidas en un `.env` que no se sube.

### Tiempo
- **RNF-04.** La fecha y hora actuales se obtienen de un reloj inyectable, nunca consultando directamente el reloj del sistema. Así las reglas que dependen del tiempo (RN-08, RN-14, RN-18, RN-21 y el pasaje a FINALIZADO) se pueden probar con una hora fija.

### Errores
- **RNF-05.** Los errores se manejan de forma centralizada, y los mensajes están en español y explican cómo resolver el problema.

### Pruebas
- **RNF-06.** Cada regla de negocio tiene al menos una prueba, incluyendo sus casos límite (por ejemplo, reservar exactamente 5 minutos antes o turnos consecutivos).
- **RNF-07.** La cobertura tiene un umbral mínimo verificado en el build. *(Valor del umbral a definir por el equipo.)*
- **RNF-10.** Los endpoints de generación de turnos, búsqueda, reserva y cancelación tienen pruebas con MockMvc que verifican el código de estado y el JSON de la respuesta, incluyendo los conflictos: reserva duplicada, horarios superpuestos, turnos pasados y cancelaciones no permitidas.

### Documentación de la API
- **RNF-11.** La API se documenta con Swagger/OpenAPI y se verifica con una colección de Postman guardada en `docs/postman/`, que incluye el login de cada rol y un acceso denegado (403) por rol.

### Accesibilidad
- **RNF-08.** Las pantallas siguen pautas básicas de accesibilidad:
  - todos los campos tienen una etiqueta asociada (`<label for>`);
  - se puede navegar con el teclado y el texto tiene buen contraste;
  - son compatibles con lectores de pantalla: idioma de la página (`lang="es"`), títulos descriptivos, texto alternativo en las imágenes y mensajes de error que se anuncian al aparecer (`role="alert"`);
  - los textos son claros: los botones dicen la acción ("Reservar turno") y los errores explican qué corregir.

### Desempeño
- **RNF-09.** Las búsquedas y listados responden en menos de 3 segundos con los datos de prueba de la demo. *(Valor a confirmar por el equipo.)*
