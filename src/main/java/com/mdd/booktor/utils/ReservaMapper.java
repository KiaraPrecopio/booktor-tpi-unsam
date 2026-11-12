package com.mdd.booktor.utils;

import com.mdd.booktor.dtos.ReservaResponse;
import com.mdd.booktor.models.Reserva;
import com.mdd.booktor.models.Turno;

public final class ReservaMapper {

    private ReservaMapper() {
    }

    public static ReservaResponse toResponse(Reserva reserva) {
        Turno turno = reserva.getTurno();
        return new ReservaResponse(
            reserva.getId(),
            turno.getId(),
            reserva.getEstudiante().getId(),
            turno.getMateria().getNombre(),
            turno.getTutor().getNombre() + " " + turno.getTutor().getApellido(),
            turno.getInicio(),
            turno.getFin(),
            turno.getModalidad(),
            turno.getLink(),
            turno.getLugar(),
            reserva.getEstado(),
            reserva.getFechaReserva(),
            reserva.getObjetivo()
        );
    }
}
