package com.mdd.booktor.exceptions;

/**
 * Un dato no cumple una regla que se controla en el service, por ejemplo un turno en el pasado
 * o una contraseña corta. Se responde con 400.
 */
public class DatoInvalidoException extends RuntimeException {

    /**
     * Crea la excepción.
     *
     * @param mensaje el motivo, que llega tal cual al cliente
     */
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
