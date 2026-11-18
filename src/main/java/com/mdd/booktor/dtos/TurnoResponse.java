package com.mdd.booktor.dtos;

import java.time.LocalDateTime;

import com.mdd.booktor.models.EstadoTurno;
import com.mdd.booktor.models.Modalidad;

public record TurnoResponse(Long id, Long tutorId, Long materiaId, String materia, LocalDateTime inicio, LocalDateTime fin,
        EstadoTurno estado, Modalidad modalidad, String link, String lugar) {
}
