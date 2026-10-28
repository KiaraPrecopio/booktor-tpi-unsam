package com.mdd.booktor.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdd.booktor.models.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
 
    List<Materia> findByActivoTrue();
    Optional<Materia> findByIdAndActivoTrue(Long id);
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(String nombre, Long id);
}
