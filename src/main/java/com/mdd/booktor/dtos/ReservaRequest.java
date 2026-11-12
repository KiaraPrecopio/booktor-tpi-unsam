package com.mdd.booktor.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReservaRequest(
    @NotNull(message = "El estudiante es obligatorio") Long estudianteId,
    @NotNull(message = "El turno es obligatorio") Long turnoId,
    @Size(max = 500, message = "El objetivo no puede superar los 500 caracteres") String objetivo
) {
}
