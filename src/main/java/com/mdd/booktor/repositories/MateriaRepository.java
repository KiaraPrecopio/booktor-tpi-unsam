package com.mdd.booktor.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mdd.booktor.models.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
 
    List<Materia> findByActivoTrue();
    Optional<Materia> findByIdAndActivoTrue(Long id);
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(String nombre, Long id);

    @Query("""
            SELECT m FROM Materia m
            WHERE m.activo = true
              AND LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
              AND EXISTS (
                  SELECT t FROM Turno t
                  WHERE t.materia = m
                    AND t.estado = com.mdd.booktor.models.EstadoTurno.DISPONIBLE
                    AND t.inicio > :ahora
                    AND t.tutor.activo = true
              )
            ORDER BY m.nombre
            """)
    List<Materia> buscarConTurnosDisponibles(@Param("nombre") String nombre, @Param("ahora") LocalDateTime ahora);
}
