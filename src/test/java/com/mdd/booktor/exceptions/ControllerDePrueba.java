package com.mdd.booktor.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

// Controller solo para pruebas: permite probar GlobalExceptionHandler sin controllers reales.
@RestController
@RequestMapping("/prueba")
class ControllerDePrueba {

    record DatosDePrueba(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotBlank(message = "El email es obligatorio") String email) {
    }

    @GetMapping("/no-encontrado")
    void noEncontrado() {
        throw new RecursoNoEncontradoException("No existe la materia con id 99");
    }

    @GetMapping("/conflicto")
    void conflicto() {
        throw new ConflictoException("No se puede dar de baja la materia: tiene turnos futuros");
    }

    // Vacío a propósito: si los datos son inválidos, Spring lanza la excepción antes de entrar.
    @PostMapping("/validacion")
    void validacion(@Valid @RequestBody DatosDePrueba datos) {
    }
}