package com.mdd.booktor.services;

import java.util.List;

import com.mdd.booktor.dtos.EstudianteRequest;
import com.mdd.booktor.dtos.EstudianteResponse;

/**
 * ABML de estudiantes. Lo usa el administrador.
 */
public interface EstudianteService {

    /**
     * Lista los estudiantes activos.
     *
     * @return los estudiantes activos
     */
    List<EstudianteResponse> listar();

    /**
     * Busca un estudiante activo por id.
     *
     * @param id id del estudiante
     * @return el estudiante encontrado
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o está dado de baja
     */
    EstudianteResponse buscarPorId(Long id);

    /**
     * Da de alta un estudiante con su contraseña encriptada con BCrypt y las adaptaciones que necesita.
     *
     * @param request datos personales, email, contraseña y ids de adaptaciones
     * @return el estudiante creado
     * @throws com.mdd.booktor.exceptions.DatoInvalidoException si falta la contraseña o tiene menos de 8 caracteres
     * @throws com.mdd.booktor.exceptions.ConflictoException si ya existe un usuario con ese email
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si alguna adaptación no existe
     */
    EstudianteResponse crear(EstudianteRequest request);

    /**
     * Modifica un estudiante activo. Si la contraseña viene vacía, conserva la anterior.
     *
     * @param id id del estudiante
     * @param request datos nuevos
     * @return el estudiante modificado
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe el estudiante o alguna adaptación
     * @throws com.mdd.booktor.exceptions.ConflictoException si otro usuario ya tiene ese email
     * @throws com.mdd.booktor.exceptions.DatoInvalidoException si la contraseña nueva tiene menos de 8 caracteres
     */
    EstudianteResponse modificar(Long id, EstudianteRequest request);

    /**
     * Da de baja lógica un estudiante (activo = false), si no tiene reservas activas.
     *
     * @param id id del estudiante
     * @throws com.mdd.booktor.exceptions.RecursoNoEncontradoException si no existe o ya está dado de baja
     * @throws com.mdd.booktor.exceptions.ConflictoException si tiene reservas activas
     */
    void darDeBaja(Long id);
}
