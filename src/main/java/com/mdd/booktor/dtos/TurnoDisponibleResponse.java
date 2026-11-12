package com.mdd.booktor.dtos;

import java.time.LocalDateTime;

import com.mdd.booktor.models.Modalidad;

public record TurnoDisponibleResponse(Long id, LocalDateTime inicio, LocalDateTime fin, Modalidad modalidad) {
}
