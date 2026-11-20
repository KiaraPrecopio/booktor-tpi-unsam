package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;

/**
 * ABML de las adaptaciones de accesibilidad que pueden ofrecer los tutores. Lo usa el administrador.
 */
public interface AdaptacionService {

    /**
     * Lista las adaptaciones activas.
     *
     * @return las adaptaciones activas
     */
    List<AdaptacionResponse> listar();

    /**
     * Busca una adaptación activa por id.
     *
     * @param id id de la adaptación
     * @return la adaptación encontrada
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o está dada de baja
     */
    AdaptacionResponse buscarPorId(Long id);

    /**
     * Da de alta una adaptación. El nombre no puede repetirse entre las activas, sin importar mayúsculas.
     *
     * @param request nombre y descripción
     * @return la adaptación creada
     * @throws com.mdd.booktor.exceptions.ConflictoException si ya existe una activa con ese nombre
     */
    AdaptacionResponse crear(AdaptacionRequest request);

    /**
     * Modifica el nombre y la descripción de una adaptación activa.
     *
     * @param id id de la adaptación
     * @param request nombre y descripción nuevos
     * @return la adaptación modificada
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o está dada de baja
     * @throws com.mdd.booktor.exceptions.ConflictoException si otra adaptación activa ya tiene ese nombre
     */
    AdaptacionResponse modificar(Long id, AdaptacionRequest request);

    /**
     * Da de baja lógica una adaptación (activo = false), si ningún tutor activo la ofrece.
     *
     * @param id id de la adaptación
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o ya está dada de baja
     * @throws com.mdd.booktor.exceptions.ConflictoException si la ofrecen tutores activos
     */
    void darDeBaja(Long id);
}
