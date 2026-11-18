package com.mdd.booktor.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mdd.booktor.models.Rol;
import com.mdd.booktor.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("""
            SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.adaptaciones
            WHERE u.rol = :rol
              AND u.activo = true
            """)
    List<Usuario> buscarActivosConAdaptaciones(@Param("rol") Rol rol);

    @Query("""
            SELECT u FROM Usuario u LEFT JOIN FETCH u.adaptaciones
            WHERE u.id = :id
              AND u.rol = :rol
              AND u.activo = true
            """)
    Optional<Usuario> buscarActivoConAdaptaciones(@Param("id") Long id, @Param("rol") Rol rol);

    List<Usuario> findByRolAndActivoTrueAndAdaptaciones_Id(Rol rol, Long adaptacionId);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCaseAndRolAndActivoTrue(String email, Rol rol);
    Optional<Usuario> findByEmailIgnoreCaseAndActivoTrue(String email);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
