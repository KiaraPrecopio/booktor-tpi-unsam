package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.TutorRequest;
import com.mdd.booktor.dtos.TutorResponse;

/**
 * ABML de tutores. Lo usa el administrador.
 */
public interface TutorService {

    /**
     * Lista los tutores activos.
     *
     * @return los tutores activos
     */
    List<TutorResponse> listar();

    /**
     * Busca un tutor activo por id.
     *
     * @param id id del tutor
     * @return el tutor encontrado
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o está dado de baja
     */
    TutorResponse buscarPorId(Long id);

    /**
     * Da de alta un tutor con su contraseña encriptada con BCrypt, la duración de sus turnos
     * y las adaptaciones que ofrece.
     *
     * @param request datos personales, email, contraseña, duración del turno e ids de adaptaciones
     * @return el tutor creado
     * @throws com.mdd.booktor.exceptions.DatoInvalidoException si falta la contraseña o tiene menos de 8 caracteres
     * @throws com.mdd.booktor.exceptions.ConflictoException si ya existe un usuario con ese email
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si alguna adaptación no existe
     */
    TutorResponse crear(TutorRequest request);

    /**
     * Modifica un tutor activo. Si la contraseña viene vacía, conserva la anterior.
     * No se puede cambiar la duración del turno si tiene turnos futuros asignados.
     *
     * @param id id del tutor
     * @param request datos nuevos
     * @return el tutor modificado
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el tutor o alguna adaptación
     * @throws com.mdd.booktor.exceptions.ConflictoException si otro usuario ya tiene ese email o no se puede cambiar la duración
     * @throws com.mdd.booktor.exceptions.DatoInvalidoException si la contraseña nueva tiene menos de 8 caracteres
     */
    TutorResponse modificar(Long id, TutorRequest request);

    /**
     * Da de baja lógica un tutor (activo = false), si sus turnos no tienen reservas activas.
     *
     * @param id id del tutor
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o ya está dado de baja
     * @throws com.mdd.booktor.exceptions.ConflictoException si sus turnos tienen reservas activas
     */
    void darDeBaja(Long id);
}
