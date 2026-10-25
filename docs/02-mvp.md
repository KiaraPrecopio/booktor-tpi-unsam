# MVP

## Entra en el MVP

### ABML de catálogos (Administrador)
- **Materias:** alta, baja lógica, modificación y listado.
- **Adaptaciones** (herramientas de asistencia o formatos alternativos): alta, baja lógica, modificación y listado.

### ABML de usuarios (Administrador)
- **Estudiantes:** alta, baja lógica, modificación y listado, con sus adaptaciones preferidas.
- **Tutores:** alta, baja lógica, modificación y listado. Cada tutor tiene:
  - las adaptaciones que ofrece (una o varias),
  - la duración de sus turnos (una sola duración por tutor).
- **Administrador:** hay un administrador inicial cargado al iniciar el sistema. No tiene ABML.

### Turnos del tutor
- El tutor **genera turnos en bloque** para una materia: por ejemplo, Matemática el martes 28/10 de 14:00 a 18:00.
- El sistema divide el bloque según la duración del tutor (con turnos de 60 minutos, el bloque anterior genera 4 turnos). Solo se guardan los turnos.
- El tutor puede dar de baja los turnos que están disponibles, con las restricciones de [03-definiciones.md](03-definiciones.md).

### Búsqueda y reservas
- **Búsqueda:** el estudiante busca materias por nombre (solo aparecen las que tienen turnos disponibles) y elige una. Ve una card por tutor, con sus adaptaciones y sus turnos disponibles, y puede filtrar los tutores por adaptación.
- **Reserva:** el estudiante reserva un turno disponible.
- **Cancelación:** el estudiante, el tutor o el administrador pueden cancelar una reserva. La reserva cancelada queda guardada y el turno vuelve a estar disponible.
- **Mis turnos:** el estudiante ve sus próximas reservas; el tutor ve sus próximos turnos, reservados o no.

### Acceso
- **Login** para estudiantes, tutores y administradores.
- Cada actor accede solo a las operaciones de su rol.

### Interfaz
- Interfaz web con Thymeleaf, con un menú de opciones según el actor.
- API REST documentada con Swagger/OpenAPI.

## Candidatos a añadir como extras

Se encaran solo con el MVP terminado y probado:

- **Recomendaciones:** sugerir tutores que coincidan con las adaptaciones preferidas del estudiante.
- **Historial de turnos** para estudiantes y tutores: turnos finalizados y reservas canceladas, con quién canceló y cuándo. Los datos ya quedan guardados gracias a la entidad Reserva y a las bajas lógicas; el extra es la vista que los muestra.
- **Mi perfil:** que cada estudiante y tutor vea y modifique sus propios datos (contraseña, adaptaciones y, el tutor, su duración de turno), sin depender del administrador.
- **Perfil del tutor:** mostrar las materias que da, obtenidas de sus turnos, sin agregar tablas.
- **Disponibilidad recurrente:** opción "repetir semanalmente hasta [fecha]" al generar un bloque. Reutiliza la misma generación y las mismas validaciones, sin cambios en el modelo de datos.
- **Adaptaciones por bloque:** que el tutor elija, al generar un bloque, cuáles de las adaptaciones de su perfil ofrece en esos turnos, con las del perfil marcadas por defecto.
- **Modalidad:** que cada turno sea virtual (con el link de la reunión) o presencial (con el lugar), y que el estudiante pueda filtrar por ella.
- **Archivar materias y adaptaciones:** que dejen de ofrecerse para lo nuevo sin afectar los turnos y los tutores que ya las usan.
