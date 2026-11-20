package com.mdd.booktor.exceptions;

/**
 * El usuario tiene sesión pero no puede hacer esa operación, por ejemplo dar de baja el turno de otro tutor.
 * Se responde con 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    /**
     * Crea la excepción.
     *
     * @param mensaje el motivo, que llega tal cual al cliente
     */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
