package com.mdd.booktor.dtos;

import java.time.LocalDateTime;

import com.mdd.booktor.models.EstadoReserva;
import com.mdd.booktor.models.Modalidad;
import com.mdd.booktor.models.Rol;

public record ReservaResponse(Long id, Long turnoId, Long estudianteId, String estudiante, String materia, String tutor,
        LocalDateTime inicio, LocalDateTime fin, Modalidad modalidad, String link, String lugar, EstadoReserva estado,
        LocalDateTime fechaReserva, String objetivo, Rol canceladaPor, LocalDateTime fechaCancelacion) {
}
