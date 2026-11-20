package com.mdd.booktor.exceptions;

/**
 * No existe el registro pedido o está dado de baja. Se responde con 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepción.
     *
     * @param mensaje qué registro no se encontró
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
