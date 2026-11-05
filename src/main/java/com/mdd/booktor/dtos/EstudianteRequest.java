package com.mdd.booktor.dtos;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstudianteRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre no puede superar los 100 caracteres") String nombre,
    @NotBlank(message = "El apellido es obligatorio") @Size(max = 100, message = "El apellido no puede superar los 100 caracteres") String apellido,
    @NotBlank(message = "El email es obligatorio") @Email(message = "El email no tiene un formato válido") @Size(max = 150, message = "El email no puede superar los 150 caracteres") String email,
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
    Set<Long> adaptacionIds
) {
}
