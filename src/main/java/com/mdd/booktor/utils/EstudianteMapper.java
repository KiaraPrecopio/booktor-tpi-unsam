package com.mdd.booktor.utils;

import java.util.Comparator;
import java.util.List;

import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.dtos.EstudianteResponse;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.Usuario;

public final class EstudianteMapper {

    private EstudianteMapper() {
    }

    public static EstudianteResponse toResponse(Usuario estudiante) {
        List<AdaptacionResponse> adaptaciones = estudiante.getAdaptaciones().stream()
                .sorted(Comparator.comparing(Adaptacion::getNombre))
                .map(AdaptacionMapper::toResponse)
                .toList();

        return new EstudianteResponse(
            estudiante.getId(),
            estudiante.getNombre(),
            estudiante.getApellido(),
            estudiante.getEmail(),
            adaptaciones
        );
    }
}
