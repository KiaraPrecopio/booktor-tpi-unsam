Feature: Reservar un turno de tutoría

  Como estudiante
  Quiero reservar un turno disponible
  Para recibir apoyo en la materia que estoy cursando


  Scenario: Reservar un turno disponible con un objetivo

    Given un turno disponible que empieza en 60 minutos
    When el estudiante reserva el turno con el objetivo "Repasar excepciones"
    Then la reserva queda activa con el objetivo "Repasar excepciones"
    And el turno queda asignado


  Scenario: Rechazar un turno que ya está asignado

    Given un turno asignado que empieza en 60 minutos
    When el estudiante intenta reservar el turno
    Then la reserva es rechazada con el mensaje "El turno no está disponible para reservar."


  Scenario Outline: Reservar con al menos 5 minutos de anticipación

    Given un turno disponible que empieza en <minutos> minutos
    When el estudiante intenta reservar el turno
    Then la reserva es <resultado>

    Examples:
      | minutos | resultado |
      | 10      | aceptada  |
      | 5       | aceptada  |
      | 4       | rechazada |


  Scenario: Rechazar un turno que se superpone con otra reserva del estudiante

    Given un turno disponible que empieza en 60 minutos
    And el estudiante ya tiene otra reserva activa en ese horario
    When el estudiante intenta reservar el turno
    Then la reserva es rechazada
    And el turno sigue disponible
