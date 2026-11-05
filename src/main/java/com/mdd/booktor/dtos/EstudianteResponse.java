package com.mdd.booktor.dtos;

import java.util.List;

public record EstudianteResponse(Long id, String nombre, String apellido, String email, List<AdaptacionResponse> adaptaciones) {
}
