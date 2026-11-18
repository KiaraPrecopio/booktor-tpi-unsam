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

// Para las páginas web. Los errores se muestran en error.html, como GlobalExceptionHandler lo hace en JSON para la API.
@ControllerAdvice(basePackages = "com.mdd.booktor.controllers.web")
public class WebControllerAdvice {

    @ExceptionHandler({ RecursoNoEncontradoException.class, ConflictoException.class,
            AccesoDenegadoException.class, DatoInvalidoException.class })
    public String errorDelNegocio(RuntimeException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error";
    }

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
