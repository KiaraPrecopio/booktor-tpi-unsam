package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.ReservaRequest;
import com.mdd.booktor.dtos.ReservaResponse;

/**
 * Reglas de negocio de las reservas de turnos.
 */
public interface ReservaService {

    /**
     * Reserva un turno para el estudiante y pasa el turno a ASIGNADO.
     * El turno tiene que estar disponible, su tutor activo, faltar al menos 5 minutos para el inicio
     * y el estudiante no puede tener otra reserva activa que se superponga en horario.
     *
     * @param request id del turno y objetivo opcional del estudiante
     * @param emailEstudiante email del estudiante que reserva, tomado del token
     * @return la reserva creada, con el link o el lugar del encuentro
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el estudiante o el turno
     * @throws com.mdd.booktor.exceptions.ConflictoException si no se cumple alguna de las reglas
     */
    ReservaResponse reservar(ReservaRequest request, String emailEstudiante);

    /**
     * Cancela una reserva activa y deja el turno disponible otra vez.
     * Pueden cancelar el estudiante que reservó, el tutor del turno o un administrador,
     * siempre que el turno todavía no haya empezado.
     *
     * @param id id de la reserva
     * @param emailUsuario email de quien cancela, tomado del token
     * @return la reserva cancelada
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe la reserva o el usuario
     * @throws com.mdd.booktor.exceptions.AccesoDenegadoException si el usuario no puede cancelar esa reserva
     * @throws com.mdd.booktor.exceptions.ConflictoException si la reserva no está activa o el turno ya empezó
     */
    ReservaResponse cancelar(Long id, String emailUsuario);

    /**
     * Lista todas las reservas activas de turnos que todavía no empezaron. La usa el administrador.
     *
     * @return las reservas ordenadas por fecha de inicio del turno
     */
    List<ReservaResponse> listarActivasFuturas();

    /**
     * Lista las reservas activas y futuras de un estudiante (pantalla Mis reservas).
     *
     * @param emailEstudiante email del estudiante, tomado del token
     * @return sus reservas ordenadas por fecha de inicio del turno
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el estudiante
     */
    List<ReservaResponse> listarProximasDeEstudiante(String emailEstudiante);
}
