package com.mdd.booktor.dtos;

import java.util.List;

public record TutorDisponibleResponse(Long id, String nombre, String apellido, List<AdaptacionResponse> adaptaciones,
        List<TurnoDisponibleResponse> turnos) {
}
