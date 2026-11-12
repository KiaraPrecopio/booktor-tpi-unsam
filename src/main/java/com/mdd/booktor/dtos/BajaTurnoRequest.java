package com.mdd.booktor.dtos;

import jakarta.validation.constraints.NotNull;

public record BajaTurnoRequest(
    @NotNull(message = "El tutor es obligatorio") Long tutorId
) {
}
