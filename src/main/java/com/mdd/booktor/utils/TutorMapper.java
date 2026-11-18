package com.mdd.booktor.utils;

import java.util.Comparator;
import java.util.List;

import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.dtos.TutorResponse;
import com.mdd.booktor.models.Adaptacion;
import com.mdd.booktor.models.Usuario;

public final class TutorMapper {

    private TutorMapper() {
    }

    public static TutorResponse toResponse(Usuario tutor) {
        List<AdaptacionResponse> adaptaciones = tutor.getAdaptaciones().stream()
                .sorted(Comparator.comparing(Adaptacion::getNombre))
                .map(AdaptacionMapper::toResponse)
                .toList();

        return new TutorResponse(
            tutor.getId(),
            tutor.getNombre(),
            tutor.getApellido(),
            tutor.getEmail(),
            tutor.getDuracionTurnoMinutos(),
            adaptaciones
        );
    }
}
