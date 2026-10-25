# Primeras definiciones

## Glosario

| Término | Definición |
|---|---|
| **Usuario** | Persona que usa el sistema, con un rol: administrador, estudiante o tutor. |
| **Materia** | Asignatura sobre la que se da la tutoría. Es un catálogo que administra el Administrador. |
| **Adaptación** | Herramienta de asistencia o formato alternativo que un tutor ofrece (por ejemplo: material visual, lectura fácil, apoyo auditivo). Es un catálogo que administra el Administrador. |
| **Duración del turno** | Minutos que dura cada turno de un tutor. Es única por tutor. |
| **Bloque** | Rango horario (fecha, hora desde y hora hasta) que el tutor carga para generar varios turnos de una vez. No se guarda. |
| **Turno** | Horario concreto de un tutor para una materia, con la duración del tutor. Es el horario en sí, esté reservado o no. |
| **Reserva** | Registro de que un estudiante tomó un turno: quién, cuándo y en qué estado está. Si se cancela, guarda también quién canceló y cuándo. |
| **Baja lógica** | El registro no se borra de la base: se marca como inactivo (en los turnos, con el estado DADO_DE_BAJA) y deja de aparecer en los listados. |

## Estados

### Turno

```
DISPONIBLE ──reserva──▶ ASIGNADO ──pasa el horario──▶ FINALIZADO
   │   ▲                   │
   │   └───cancelación─────┘
   └──baja──▶ DADO_DE_BAJA
```

| Estado | Significado |
|---|---|
| **DISPONIBLE** | El turno está libre y se puede reservar. |
| **ASIGNADO** | El turno tiene una reserva ACTIVA. |
| **FINALIZADO** | El horario del turno ya pasó y el turno estaba asignado. |
| **DADO_DE_BAJA** | El tutor dio de baja el turno cuando estaba DISPONIBLE. Ya no se ofrece. |

La baja de un turno es lógica: el registro no se borra, pasa al estado DADO_DE_BAJA.

### Reserva

```
ACTIVA ──pasa el horario──▶ FINALIZADA
   │
   └──cancelación──▶ CANCELADA
```

| Estado | Significado |
|---|---|
| **ACTIVA** | El estudiante tiene el turno reservado. |
| **CANCELADA** | La reserva se canceló. Queda guardada con quién la canceló y cuándo. |
| **FINALIZADA** | El turno se cumplió. |

Un mismo turno (un horario de un tutor) puede acumular varias reservas a lo largo del tiempo, pero solo una puede estar ACTIVA a la vez. Por ejemplo: el turno del martes de 14:00 a 14:30 con la tutora Ana lo reserva Juan, Juan cancela (su reserva queda CANCELADA) y después lo reserva María (nueva reserva ACTIVA). Lo que no puede pasar es que Juan y María tengan ese mismo turno reservado al mismo tiempo.

Esto no limita al estudiante: puede tener todas las reservas ACTIVAS que quiera en turnos distintos, siempre que no se superpongan (RN-16). Por ejemplo, puede reservar de 14:00 a 14:30 y de 14:30 a 15:00, porque un turno termina justo cuando empieza el otro.

### Pasaje a FINALIZADO

Es automático: si un turno ASIGNADO no se canceló y su horario ya terminó, el turno pasa a FINALIZADO y su reserva a FINALIZADA, sin que nadie lo marque.

Lo dispara una tarea programada (`@Scheduled`) que corre cada cierto tiempo (por ejemplo, cada 5 minutos) y pasa a FINALIZADO los turnos que ya terminaron. Así la base siempre refleja el estado real y las consultas son solo lecturas. La tarea toma la hora del reloj inyectable, para poder probarla con una hora fija.

Como entre una corrida y la siguiente puede haber turnos terminados que todavía figuran ASIGNADOS, la búsqueda nunca muestra turnos cuyo horario ya pasó, aunque su estado todavía no se haya actualizado (RN-18).

## Reglas de negocio

### Usuarios
- **RN-01.** El email es obligatorio y no se repite entre usuarios.
- **RN-02.** Cada usuario tiene un solo rol: administrador, estudiante o tutor. Solo los tutores tienen duración de turno, que es obligatoria, en minutos y mayor a cero.
- **RN-03.** Cada operación valida el rol del usuario que involucra: los turnos son de tutores, las reservas son de estudiantes, y las adaptaciones son preferidas por estudiantes u ofrecidas por tutores.
- **RN-04.** La duración de turno de un tutor solo se puede cambiar si no tiene turnos futuros ASIGNADOS. Los turnos ya generados conservan su horario.

### Catálogos
- **RN-05.** El nombre de una materia o adaptación es obligatorio y no se repite entre las activas.

### Turnos
- **RN-06.** El tutor genera sus turnos en bloque: indica materia, fecha, hora desde y hora hasta, y el sistema divide ese rango en turnos según su duración. Los turnos quedan DISPONIBLES. El bloque no se guarda: solo se guardan los turnos.
- **RN-07.** La hora hasta del bloque tiene que ser posterior a la hora desde.
- **RN-08.** No se pueden generar turnos en el pasado.
- **RN-09.** La duración del bloque tiene que ser múltiplo de la duración del turno del tutor, para que no queden minutos sueltos.
- **RN-10.** Cada turno es de una sola materia, que tiene que estar activa. La reserva es de la materia de su turno.
- **RN-11.** Ningún turno nuevo puede superponerse con los turnos del tutor que no estén DADOS_DE_BAJA, aunque sean de otra materia. Si alguno se superpone, el sistema rechaza el bloque entero e indica con qué turnos choca.
- **RN-12.** El tutor puede dar de baja sus turnos DISPONIBLES: pasan a DADO_DE_BAJA y dejan de ofrecerse. Un turno ASIGNADO no se puede dar de baja: primero hay que cancelar su reserva.

### Reservas
- **RN-13.** Solo se pueden reservar turnos DISPONIBLES. Al reservar, se crea una reserva ACTIVA y el turno pasa a ASIGNADO.
- **RN-14.** No se puede reservar un turno que ya empezó. Se puede reservar hasta 5 minutos antes del inicio.
- **RN-15.** El tutor del turno y el estudiante que reserva tienen que estar activos.
- **RN-16.** Un estudiante no puede tener dos reservas ACTIVAS que se superpongan en el horario. Si lo intenta, el sistema rechaza la reserva e informa con qué turno se superpone.
- **RN-17.** Un turno tiene como máximo una reserva ACTIVA.

### Búsqueda y listados
- **RN-18.** La búsqueda de turnos para reservar excluye los turnos cuyo horario ya pasó, comparando su inicio con la hora actual y sin depender del estado del turno.
- **RN-19.** La búsqueda de materias es por nombre y muestra solo las materias activas que tienen turnos disponibles: turnos DISPONIBLES, futuros y de tutores activos.
- **RN-20.** Al elegir una materia, el estudiante ve los tutores que tienen turnos disponibles en ella, con sus adaptaciones y sus turnos. Puede filtrar los tutores por adaptación: si elige varias, ve solo los tutores que ofrecen **todas** las elegidas.

### Cancelaciones
- **RN-21.** Solo se pueden cancelar reservas ACTIVAS de turnos que todavía no empezaron. La reserva pasa a CANCELADA, se registra quién canceló y cuándo, y el turno vuelve a DISPONIBLE.
- **RN-22.** Pueden cancelar el estudiante que reservó, el tutor del turno o un administrador.

### Bajas
- **RN-23.** Todas las bajas son lógicas, para mantener la trazabilidad.
- **RN-24.** No se puede dar de baja a un tutor o estudiante que tenga reservas ACTIVAS en turnos futuros. El sistema rechaza la baja e indica qué reservas hay que cancelar primero.
- **RN-25.** Al dar de baja a un tutor, sus turnos futuros DISPONIBLES dejan de ofrecerse, porque la búsqueda solo muestra tutores activos (RN-19).
- **RN-26.** No se puede dar de baja una materia que tenga turnos futuros DISPONIBLES o ASIGNADOS, ni una adaptación asociada a tutores activos. El sistema rechaza la baja e indica qué turnos o tutores la usan.

### Respuesta ante un conflicto
Cuando una operación se rechaza por el estado del sistema (RN-16, RN-24, RN-26 y similares), la respuesta usa el código **409 Conflict** y un mensaje que explica el motivo y, si corresponde, qué registros lo provocan.

## Decisiones tomadas

| Decisión | Motivo |
|---|---|
| Habrá un Administrador. | Gestiona los catálogos de materias y adaptaciones, y puede intervenir en reservas, usuarios y bajas. |
| Todos los usuarios están en una sola tabla, con su rol. | El modelo de datos queda simple. Las validaciones por rol (RN-02, RN-03) las hace el servicio. |
| Ninguna credencial está en el repositorio. | Los datos de la base y del administrador inicial se leen de variables de entorno (un `.env` que no se sube). |
| El esquema de la base se maneja con Liquibase. | Cada cambio queda versionado y el equipo tiene siempre la misma base. |
| La hora actual se obtiene de un reloj inyectable. | Las reglas que dependen del tiempo se pueden probar con una hora fija. |
| Materias y adaptaciones son ABML, no un catálogo fijo. | Permite que el sistema sea dinámico. |
| Las materias del tutor no se guardan en su perfil: se obtienen de sus turnos. | Evita una tabla y reglas redundantes, ya que cada turno tiene su materia. |
| Las adaptaciones están en el perfil del tutor y valen para todos sus turnos. | Mantiene acotado el MVP. Elegirlas por bloque queda como extra. |
| El filtro por adaptaciones exige que el tutor ofrezca todas las elegidas. | Un estudiante que necesita varias adaptaciones solo ve tutores que le sirven. |
| Una sola duración de turno por tutor. | Mantiene acotado el MVP. |
| El tutor genera los turnos en bloque, y solo se guardan los turnos. | Es cómodo para el tutor y evita una entidad franja con su ABML y la regeneración de turnos. La baja es por turno. |
| Cada bloque es de una fecha concreta. | Evita definir repeticiones, vencimientos y feriados. La recurrencia semanal queda como extra y se puede sumar sin cambiar el modelo. |
| El estudiante elige entre turnos concretos. | No tiene que calcular horarios. |
| Turno y reserva son entidades separadas. | La cancelación libera el turno sin perder el historial de quién lo reservó y quién canceló. |
| El pasaje a FINALIZADO es automático. | Nadie tiene que acordarse de cerrar los turnos. |
| El pasaje a FINALIZADO lo hace una tarea programada (`@Scheduled`). | La base siempre refleja el estado real y las consultas no modifican datos. |
| La búsqueda excluye los turnos pasados comparando su inicio con la hora actual. | Funciona bien sin importar cuándo se actualicen los estados. |
| La superposición de reservas del estudiante se rechaza, sin pedir confirmación. | Es más simple de implementar y de usar. |
| Las bajas con dependencias se bloquean con un mensaje explicativo (409). | Es consistente en todo el sistema y le dice al usuario qué hacer. |
