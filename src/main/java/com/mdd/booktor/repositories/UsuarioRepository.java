package com.mdd.booktor.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "adaptaciones")
    List<Usuario> findByRolAndActivoTrue(Rol rol);

    @EntityGraph(attributePaths = "adaptaciones")
    Optional<Usuario> findByIdAndRolAndActivoTrue(Long id, Rol rol);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
