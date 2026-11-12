package com.mdd.booktor.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Turno;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    @Query("""
            SELECT t FROM Turno t
            WHERE t.tutor.id = :tutorId
              AND t.estado <> com.mdd.booktor.models.EstadoTurno.DADO_DE_BAJA
              AND t.inicio < :fin
              AND t.fin > :inicio
            ORDER BY t.inicio
            """)
    List<Turno> buscarSuperpuestos(@Param("tutorId") Long tutorId, @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT t FROM Turno t JOIN FETCH t.tutor
            WHERE t.materia.id = :materiaId
              AND t.estado IN :estados
              AND t.inicio > :desde
            """)
    List<Turno> buscarFuturosDeMateria(@Param("materiaId") Long materiaId, @Param("estados") List<EstadoTurno> estados,
            @Param("desde") LocalDateTime desde);
}
