package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.BloqueTurnosRequest;
import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.dtos.TurnoTutorResponse;

/**
 * Reglas de negocio de los turnos que publican los tutores.
 */
public interface TurnoService {

    /**
     * Divide un bloque horario en turnos de la duración configurada para el tutor y los guarda como DISPONIBLES.
     * El bloque no puede estar en el pasado, su duración tiene que ser múltiplo de la duración del turno,
     * los virtuales necesitan link, los presenciales lugar, y no puede superponerse con otros turnos del tutor.
     *
     * @param request materia, fecha, hora desde, hora hasta, modalidad y link o lugar
     * @param emailTutor email del tutor, tomado del token
     * @return los turnos creados
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el tutor o la materia
     * @throws com.mdd.booktor.exceptions.DatoInvalidoException si el bloque tiene datos inválidos
     * @throws com.mdd.booktor.exceptions.ConflictoException si el bloque se superpone con otros turnos del tutor
     */
    List<TurnoResponse> generarBloque(BloqueTurnosRequest request, String emailTutor);

    /**
     * Lista los turnos futuros del tutor (disponibles y asignados), con la reserva y el objetivo de cada asignado.
     *
     * @param emailTutor email del tutor, tomado del token
     * @return sus turnos ordenados por fecha de inicio
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el tutor
     */
    List<TurnoTutorResponse> listarProximosDeTutor(String emailTutor);

    /**
     * Da de baja un turno disponible del propio tutor (pasa a DADO_DE_BAJA).
     *
     * @param id id del turno
     * @param emailTutor email del tutor, tomado del token
     * @return el turno dado de baja
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el tutor o el turno
     * @throws com.mdd.booktor.exceptions.AccesoDenegadoException si el turno es de otro tutor
     * @throws com.mdd.booktor.exceptions.ConflictoException si el turno tiene una reserva activa o no está disponible
     */
    TurnoResponse darDeBaja(Long id, String emailTutor);

    /**
     * Pasa a FINALIZADO los turnos asignados cuyo horario ya terminó, y su reserva activa a FINALIZADA.
     * La llama {@link com.mdd.booktor.tasks.FinalizarTurnosTask} cada 5 minutos.
     */
    void finalizarTurnosTerminados();
}
