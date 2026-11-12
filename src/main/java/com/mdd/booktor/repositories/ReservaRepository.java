package com.mdd.booktor.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            SELECT r FROM Reserva r JOIN FETCH r.turno t JOIN FETCH t.materia
            WHERE r.estudiante.id = :estudianteId
              AND r.estado = com.mdd.booktor.models.EstadoReserva.ACTIVA
              AND t.inicio < :fin
              AND t.fin > :inicio
            """)
    List<Reserva> buscarActivasSuperpuestas(@Param("estudianteId") Long estudianteId, @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT r FROM Reserva r JOIN FETCH r.turno t JOIN FETCH t.materia
            WHERE r.estudiante.id = :estudianteId
              AND r.estado = com.mdd.booktor.models.EstadoReserva.ACTIVA
              AND t.inicio > :ahora
            ORDER BY t.inicio
            """)
    List<Reserva> buscarActivasFuturasDeEstudiante(@Param("estudianteId") Long estudianteId, @Param("ahora") LocalDateTime ahora);

    @Query("""
            SELECT r FROM Reserva r JOIN FETCH r.turno t JOIN FETCH t.materia
            WHERE t.tutor.id = :tutorId
              AND r.estado = com.mdd.booktor.models.EstadoReserva.ACTIVA
              AND t.inicio > :ahora
            ORDER BY t.inicio
            """)
    List<Reserva> buscarActivasFuturasDeTutor(@Param("tutorId") Long tutorId, @Param("ahora") LocalDateTime ahora);

    @Query("""
            SELECT r FROM Reserva r JOIN FETCH r.turno t JOIN FETCH t.materia JOIN FETCH t.tutor JOIN FETCH r.estudiante
            WHERE r.estado = com.mdd.booktor.models.EstadoReserva.ACTIVA
              AND t.inicio > :ahora
            ORDER BY t.inicio
            """)
    List<Reserva> buscarActivasFuturas(@Param("ahora") LocalDateTime ahora);

    Optional<Reserva> findByTurno_IdAndEstado(Long turnoId, EstadoReserva estado);
}
