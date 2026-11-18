package com.mdd.booktor.utils;

import com.mdd.booktor.dtos.AdaptacionRequest;
import com.mdd.booktor.dtos.AdaptacionResponse;
import com.mdd.booktor.models.Adaptacion;

public final class AdaptacionMapper {
    private AdaptacionMapper() {
    }

    public static AdaptacionResponse toResponse(Adaptacion adaptacion) {
        return new AdaptacionResponse(
            adaptacion.getId(),
            adaptacion.getNombre(),
            adaptacion.getDescripcion()
        );
    }

    public static Adaptacion toEntity(AdaptacionRequest request) {
        return new Adaptacion(
            request.nombre(),
            request.descripcion(),
            true
        );
    }
}

