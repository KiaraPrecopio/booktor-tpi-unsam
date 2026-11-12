package com.mdd.booktor.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import com.mdd.booktor.models.Modalidad;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BloqueTurnosRequest(
    @NotNull(message = "El tutor es obligatorio") Long tutorId,
    @NotNull(message = "La materia es obligatoria") Long materiaId,
    @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
    @NotNull(message = "La hora desde es obligatoria") LocalTime horaDesde,
    @NotNull(message = "La hora hasta es obligatoria") LocalTime horaHasta,
    @NotNull(message = "La modalidad es obligatoria") Modalidad modalidad,
    @Size(max = 255, message = "El link no puede superar los 255 caracteres") String link,
    @Size(max = 255, message = "El lugar no puede superar los 255 caracteres") String lugar
) {
}
