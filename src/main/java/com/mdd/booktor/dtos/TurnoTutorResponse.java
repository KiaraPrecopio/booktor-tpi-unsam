package com.mdd.booktor.dtos;

import java.time.LocalDateTime;

import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Modalidad;

public record TurnoTutorResponse(Long id, String materia, LocalDateTime inicio, LocalDateTime fin, EstadoTurno estado,
        Modalidad modalidad, String link, String lugar, Long reservaId, String estudiante, String objetivo) {
}
