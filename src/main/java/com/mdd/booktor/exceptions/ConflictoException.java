package com.mdd.booktor.exceptions;

// El mensaje explica el motivo y qué registros provocan el conflicto,
// porque llega tal cual al cliente.
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
