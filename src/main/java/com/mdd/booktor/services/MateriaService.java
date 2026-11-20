package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;

/**
 * ABML de las materias en las que se ofrecen tutorías. Lo usa el administrador.
 */
public interface MateriaService {

    /**
     * Lista las materias activas.
     *
     * @return las materias activas
     */
    List<MateriaResponse> listar();

    /**
     * Busca una materia activa por id.
     *
     * @param id id de la materia
     * @return la materia encontrada
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o está dada de baja
     */
    MateriaResponse buscarPorId(Long id);

    /**
     * Da de alta una materia. El nombre no puede repetirse entre las activas, sin importar mayúsculas.
     *
     * @param request nombre y descripción
     * @return la materia creada
     * @throws com.mdd.booktor.exceptions.ConflictoException si ya existe una activa con ese nombre
     */
    MateriaResponse crear(MateriaRequest request);

    /**
     * Modifica el nombre y la descripción de una materia activa.
     *
     * @param id id de la materia
     * @param request nombre y descripción nuevos
     * @return la materia modificada
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o está dada de baja
     * @throws com.mdd.booktor.exceptions.ConflictoException si otra materia activa ya tiene ese nombre
     */
    MateriaResponse modificar(Long id, MateriaRequest request);

    /**
     * Da de baja lógica una materia (activo = false), si no tiene turnos futuros.
     *
     * @param id id de la materia
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o ya está dada de baja
     * @throws com.mdd.booktor.exceptions.ConflictoException si tiene turnos futuros
     */
    void darDeBaja(Long id);
}
