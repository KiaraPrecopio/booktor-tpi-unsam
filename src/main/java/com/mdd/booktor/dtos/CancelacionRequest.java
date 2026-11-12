package com.mdd.booktor.dtos;

import jakarta.validation.constraints.NotNull;

public record CancelacionRequest(
    @NotNull(message = "El usuario que cancela es obligatorio") Long usuarioId
) {
}
