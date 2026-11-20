package com.mdd.booktor.services;

import java.util.List;
import java.util.Set;

import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.dtos.TutorDisponibleResponse;
import com.mdd.booktor.models.Modalidad;

/**
 * Búsqueda de tutorías que hace el estudiante: primero la materia y después los tutores.
 */
public interface BusquedaService {

    /**
     * Busca las materias activas que tienen turnos disponibles a futuro.
     *
     * @param nombre parte del nombre de la materia; vacío o nulo trae todas
     * @return las materias ordenadas por nombre
     */
    List<MateriaResponse> buscarMaterias(String nombre);

    /**
     * Lista los tutores con turnos disponibles a futuro para una materia, con esos turnos.
     * El link y el lugar no se incluyen: solo los ve quien reserva.
     *
     * @param materiaId id de la materia
     * @param adaptacionIds adaptaciones que el tutor tiene que ofrecer todas; vacío o nulo no filtra
     * @param modalidad modalidad de los turnos; nula no filtra
     * @return los tutores que cumplen los filtros, con sus turnos
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe la materia
     */
    List<TutorDisponibleResponse> buscarTutores(Long materiaId, Set<Long> adaptacionIds, Modalidad modalidad);
}
