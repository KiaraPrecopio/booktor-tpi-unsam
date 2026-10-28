package com.mdd.booktor.utils;

import com.mdd.booktor.dtos.MateriaRequest;
import com.mdd.booktor.dtos.MateriaResponse;
import com.mdd.booktor.models.Materia;

// Convierte entre la entidad Materia y sus DTOs
public final class MateriaMapper {

    private MateriaMapper() {
    }

    public static MateriaResponse toResponse(Materia materia) {
        return new MateriaResponse(materia.getId(), materia.getNombre(), materia.getDescripcion());
    }

    public static Materia toEntity(MateriaRequest request) {
        return new Materia(request.nombre(), request.descripcion(), true);
    }
}