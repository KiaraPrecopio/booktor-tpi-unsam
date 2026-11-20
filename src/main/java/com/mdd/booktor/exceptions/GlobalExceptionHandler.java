package com.mdd.booktor.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de los services a respuestas HTTP con el formato estándar {@link ProblemDetail}.
 * Solo atiende a los {@code @RestController}: las páginas usan
 * {@link com.mdd.booktor.controllers.web.WebControllerAdvice}.
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /**
     * Responde 404.
     *
     * @param ex la excepción lanzada por el service
     * @return el error con el mensaje
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Responde 409.
     *
     * @param ex la excepción lanzada por el service
     * @return el error con el mensaje
     */
    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Responde 403.
     *
     * @param ex la excepción lanzada por el service
     * @return el error con el mensaje
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail accesoDenegado(AccesoDenegadoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Responde 400.
     *
     * @param ex la excepción lanzada por el service
     * @return el error con el mensaje
     */
    @ExceptionHandler(DatoInvalidoException.class)
    public ProblemDetail datoInvalido(DatoInvalidoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Responde 401 cuando el login falla, sin decir si lo incorrecto fue el email o la contraseña.
     *
     * @param ex la excepción de Spring Security
     * @return el error con un mensaje genérico
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail credencialesInvalidas(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }

    /**
     * Responde 400 cuando falla Bean Validation ({@code @Valid}), con el error de cada campo.
     *
     * @param ex la excepción con los campos inválidos
     * @return el error con el mapa campo → mensaje en la propiedad {@code errores}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Hay datos inválidos");
        problema.setProperty("errores", errores);
        return problema;
    }
}