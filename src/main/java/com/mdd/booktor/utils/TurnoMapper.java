package com.mdd.booktor.utils;

import com.mdd.booktor.dtos.TurnoResponse;
import com.mdd.booktor.models.Turno;

public final class TurnoMapper {

    private TurnoMapper() {
    }

    public static TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(
            turno.getId(),
            turno.getTutor().getId(),
            turno.getMateria().getId(),
            turno.getMateria().getNombre(),
            turno.getInicio(),
            turno.getFin(),
            turno.getEstado(),
            turno.getModalidad(),
            turno.getLink(),
            turno.getLugar()
        );
    }
}
