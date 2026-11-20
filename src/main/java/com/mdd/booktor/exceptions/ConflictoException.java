package com.mdd.booktor.exceptions;

/**
 * La operación choca con una regla de negocio: nombre repetido, turno ya reservado, horarios superpuestos.
 * Se responde con 409. El mensaje explica el motivo y qué registros provocan el conflicto,
 * porque llega tal cual al cliente.
 */
public class ConflictoException extends RuntimeException {

    /**
     * Crea la excepción.
     *
     * @param mensaje el motivo y los registros en conflicto
     */
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
