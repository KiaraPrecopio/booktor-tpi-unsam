package com.mdd.booktor.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdd.booktor.models.Adaptacion;

public interface AdaptacionRepository extends JpaRepository<Adaptacion, Long> {
    List<Adaptacion> findByActivoTrue();
    Optional<Adaptacion> findByIdAndActivoTrue(Long id);
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndActivoTrueAndIdNot(String nombre, Long id);
    List<Adaptacion> findByIdInAndActivoTrue(Set<Long> ids);
}
