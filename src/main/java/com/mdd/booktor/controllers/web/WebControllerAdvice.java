package com.mdd.booktor.controllers.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mdd.booktor.exceptions.AccesoDenegadoException;
import com.mdd.booktor.exceptions.ConflictoException;
import com.mdd.booktor.exceptions.DatoInvalidoException;
import com.mdd.booktor.exceptions.RecursoNoEncontradoException;

/**
 * Manejo de errores de las pantallas: muestra los errores en {@code error.html},
 * como {@link com.mdd.booktor.exceptions.GlobalExceptionHandler} lo hace en JSON para la API.
 */
@ControllerAdvice(basePackages = "com.mdd.booktor.controllers.web")
public class WebControllerAdvice {

    /** Muestra el mensaje de la excepción que lanzó el service. */
    @ExceptionHandler({ RecursoNoEncontradoException.class, ConflictoException.class,
            AccesoDenegadoException.class, DatoInvalidoException.class })
    public String errorDelNegocio(RuntimeException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }

    /** Muestra la lista de campos inválidos cuando falla Bean Validation ({@code @Valid}). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String datosInvalidos(MethodArgumentNotValidException ex, Model model) {
        List<String> errores = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.add(error.getDefaultMessage());
        }
        model.addAttribute("mensaje", "Hay datos inválidos.");
        model.addAttribute("errores", errores);
        return "error";
    }
}
